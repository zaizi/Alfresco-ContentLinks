# Alfresco RedLink Plugin

Alfresco module that allows to analyse the content of documents in order to extract information from them like persons, organizations, places, etc. 
By default only documents under a folder with the specific Enhance rule are processed and only persons, organizations and places are extracted from documents.

A scheduled job is used to enhance the content of the uploaded documents so the extracted information is not shown directly when uploading a new document.

The module consists in two components:

*     Alfresco component: Contains all the needed components to enhance a document and store the extracted information.
*     Share component: Allows to show the extracted information for each document and perform faceted search over such information

## Dependencies
* Alfresco >= 4.2
* [Redlink Java SDK](http://dev.redlink.io/sdk#java-installation) >= 1.0.0-BETA

## Installation
* Execute the `mvn clean install` command in the root directory of the project
* alfresco-redlink-module.amp and share-redlink-module.amp are generated in their respective *target* folder
* Install the amps in Alfresco using the *-force* option to avoid conflicts with some dependencies if exist

## Configuration

* RedLink configuration: Configure the next properties in alfresco-global.properties for the integration with RedLink
     * **sensefy.redlink.apiKey**: The RedLink API Key
     * **sensefy.redlink.analysisName**: The name of the RedLink analysis used to enhance the content

* Alfresco Share:
     * Create a new folder in some site
     * Create a new rule for this folder selecting the ***Enhance Content*** action and ***Execute rule in background*** option


## Execution
In order to test the plugin, do the following:

* Upload a document (or some documents) to the folder with the configured rule
* A scheduled job is executed periodically to extract the configured information from the documents
* Perform a search using the input box at the top-right of the screen
* The search results are shown and the extracted information is also shown in the left side of the screen as facets
* Select/Deselect the facets to trigger a new search filtering the documents by the selected facets
