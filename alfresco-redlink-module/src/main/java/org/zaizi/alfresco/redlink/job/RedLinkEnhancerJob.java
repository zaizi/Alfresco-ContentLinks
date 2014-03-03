/**
 * This file is part of Alfresco RedLink Module.
 *
 * Alfresco RedLink Module is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco RedLink Module is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Alfresco RedLink Module.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.zaizi.alfresco.redlink.job;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.action.scheduled.InvalidCronExpression;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.zaizi.alfresco.redlink.domain.EnhancementDefinition;
import org.zaizi.alfresco.redlink.domain.EnhancementType;
import org.zaizi.alfresco.redlink.service.EnhancerService;

public class RedLinkEnhancerJob {

    private static final Logger LOG = Logger.getLogger(RedLinkEnhancerJob.class);

    private static final String SERVICEREGISTRY_KEY = "ServiceRegistry";
    private static final String ENHANCERSERVICE_KEY = "EnhancerService";

    private ServiceRegistry serviceRegistry;
    private Scheduler scheduler;
    private EnhancerService enhancerService;
    private RepositoryState repositoryState;
    private String cronExpression;
    private int numberOfDocumentsToProcess;

    public void setRepositoryState(RepositoryState repositoryState) {
        this.repositoryState = repositoryState;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public void setNumberOfDocumentsToProcess(int numberOfDocumentsToProcess) {
        this.numberOfDocumentsToProcess = numberOfDocumentsToProcess;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Inject the enhancer service
     * 
     * @param enhancerService
     *            Enhancer service
     */
    public void setEnhancerService(EnhancerService enhancerService) {
        this.enhancerService = enhancerService;
    }

    /**
     * Set the scheduler
     * 
     * @param scheduler
     *            Scheduler
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public static final class JobImpl implements Job {
        public static final String HELPER_KEY = "helper";

        public void execute(JobExecutionContext ctx) throws JobExecutionException {
            final ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getJobDetail().getJobDataMap()
                    .get(SERVICEREGISTRY_KEY);
            final EnhancerService enhancerService = (EnhancerService) ctx.getJobDetail().getJobDataMap()
                    .get(ENHANCERSERVICE_KEY);
            final JobLockService jobLockService = serviceRegistry.getJobLockService();
            final SearchService searchService = serviceRegistry.getSearchService();
            final TransactionService transactionService = serviceRegistry.getTransactionService();
            final RedLinkEnhancerJob redlinkEnhancerJob = (RedLinkEnhancerJob) ctx.getJobDetail()
                    .getJobDataMap().get(JobImpl.HELPER_KEY);

            final long LOCK_TTL = redlinkEnhancerJob.numberOfDocumentsToProcess * 5000; // 5 seconds per
                                                                                        // document to be
                                                                                        // processed

            LOG.debug("Starting RedLinkEnhancerJob");
            if (redlinkEnhancerJob.repositoryState.isBootstrapping()) {
                LOG.debug("Repository is still boostrapping. Skipping Job");
                return;
            }

            final QName lockQName = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI,
                "RedLinkEnhancer");
            final String lockToken;
            try {
                lockToken = jobLockService.getLock(lockQName, LOCK_TTL);
            } catch (LockAcquisitionException e) {
                LOG.debug("Another RedLinkEnhancerJob job is running. Stopping");
                // Another job is being executed. Stop it!
                return;
            }

            AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
                @Override
                public Void doWork() throws Exception {
                    ResultSet rs = null;
                    List<NodeRef> nodes = null;

                    try {
                        // search parameters
                        SearchParameters sp = new SearchParameters();
                        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                        sp.setLimitBy(LimitBy.FINAL_SIZE);
                        sp.setLimit(10);
                        sp.setQuery("+ASPECT:sensefy\\:enhanced +@sensefy\\:enhanced_processed:false");

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Search Parameters: " + sp);
                        }

                        rs = searchService.query(sp);
                        nodes = rs.getNodeRefs();
                        if (nodes == null) nodes = new ArrayList<NodeRef>();
                        LOG.debug("Nodes to be enhanced: " + nodes.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (rs != null) {
                            rs.close();
                        }
                    }

                    try {
                        for (final NodeRef nodeRef : nodes) {
                            transactionService.getRetryingTransactionHelper().doInTransaction(
                                new RetryingTransactionCallback<Void>() {
                                    @Override
                                    public Void execute() throws Throwable {
                                        EnhancementDefinition def = new EnhancementDefinition();
                                        def.add(EnhancementType.LANGUAGE_DETECTION);
                                        def.add(EnhancementType.ENTITIES_EXTRACTION);
                                        enhancerService.enhance(nodeRef, def);
                                        return null;
                                    }
                                }, false, true);
                        }
                    } catch (Exception e) {
                        // An error occurred enhancing the nodes
                        LOG.debug("Error ocurred enhancing nodes");
                    } finally {
                        jobLockService.releaseLock(lockToken, lockQName);
                     
                    }
                    return null;
                
                }
                
                
            });

        }
    }

    public void startJob() throws Exception {
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(JobImpl.HELPER_KEY, this);
        jobDataMap.put(SERVICEREGISTRY_KEY, serviceRegistry);
        jobDataMap.put(ENHANCERSERVICE_KEY, enhancerService);

        JobDetail jobDetail = new JobDetail();
        jobDetail.setName(getClass().getSimpleName());
        jobDetail.setGroup("RedLinkEnhancerGroup");
        jobDetail.setJobDataMap(jobDataMap);
        jobDetail.setJobClass(JobImpl.class);

        final Trigger trigger;
        try {
            trigger = new CronTrigger(jobDetail.getName(), jobDetail.getGroup(), cronExpression);
        } catch (final ParseException e) {
            throw new InvalidCronExpression("Invalid cron expression: " + cronExpression);
        }

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
        LOG.info(getClass().getSimpleName() + " has started (cronExpression=" + cronExpression + ")");
    }

}
