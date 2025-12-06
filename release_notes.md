# Release Notes - openMDM(R) Application #

* [mdmbl Eclipse Project Page](https://projects.eclipse.org/projects/automotive.mdmbl)
* [mdmbl Eclipse Git Repositories](https://gitlab.eclipse.org/eclipse/mdmbl)
* [mdmbl nightly builds - last stable version](http://download.eclipse.org/mdmbl/nightly_master/?d)

## Version 5.4.0M1

### Changes ###

* Updated to Java 17, Jakarta EE 10 and Glassfish 7

* Changed namspace from javax.* to jakarta.*
* Added openmdm docker image to build
* Updated all java dependencies to newst versions
* Updated Gradle to 8.14
* Refactored build scripts
* Updated to node 22.14.0 and npm 11.2.0
* Added dependency check for java dependencies

* Freetextindexer:
  - Migrated to java.net.HttpClient
  - Added support for batch delete in elasticsearch

* ODS Adapter: Removed deprecated notification types: use ods_polling

* REST-Api
  - Removed deprecated "localize"-methods
  - Removed unused query parameters (DataSource, ProjectDomain, VirtualNodes)
  - Cleaned up endpoints
  
* Authentication
  - SAML2 support removed
  - Reduced web.xml by handling authentication filter by system properties
  - Added documentation to mandatory set-default-realm task in case of basic authentication
  - Basic authentication can now also be set by system properties
  - Added system property "org.eclipse.mdm.auth.method" (can be "basic" (default) or "oidc")
  - Changed system property "org.eclipse.mdm.oidc.clients" to "org.eclipse.mdm.auth.clients"
  - Changed system property "org.eclipse.mdm.oidc.role-mappings" to "org.eclipse.mdm.auth.role-mappings"
  - Changed system property "org.eclipse.mdm.oidc.contextRoot" to "org.eclipse.mdm.auth.contextRoot" (mandatory for both methods)
  - Changes brought into GettingStartedGuide
  
* Details view
  - Preference hide_ordered was removes. Instead there is a new preference for configuring whether ordered and/or measured context should be shown on Test, TestStep or Measurement level (see UserGuide -> chapter Administration)

## Version 5.3.0

### Features ###

* ODS adapter supports ASAM ODS HTTP API
* Web-Client: Added Catalogmanager & Templatemanager
* Web-Client: Support for persisting component states (e.g. xy-chartviewer, shoppin baskets, channel lists, unit mappings, ...)
* Web-Client: Support for sharing component states with URLs (e.g. search filter, table views)

Hint:
* Corba File server will not be used by the HTTP API. Thus files referenced as DT/DS_ExternalReference can not be downloaded anymore. Please migrate to AoFiles.

### Changes ###

* Support sequence attributes in nodeprovider virtual nodes
* Updated to gradle 8.3
* Harmonized jackson-databind version to 2.9.2
* CSV-Import: Relaxed check if unit matches
* Added support for repeating virtual node types in GenericNodeProvider
* Navigator can now store state for several nodeproviders
* Switched from ambiguous EntityStore#getCurrent() to #getCurrentMap()
* Test.ResponsibleUser is now optional
* Fixed Test and TestStep queries with Status attribute
* Support querying for files
* Fixed redundant MDMLink-import in csv adapter
* Fixed atfx export with units in catalog attributes
* OIDC integration
* Added loadWithoutChildren() methods to EntityManager
* Added FileRelation-Endpoint to download several Files as one zip
* Changed URLEncoding of FileLinks due to Import errors
* Switched from shared ParameterSet (ResultParameterSet) to dedicated Measurement- and ChannelParameterSet EntityTypes to support better querying. ParameterSet can still be used, but new users should switch to Measurement- and ChannelParameterSets.
* Added mdm_complete_application_model_511.atfx with additional application elements MeaResultParameterSet, MeaResultParamter, MeaQuantityParameterSet and MeaQuantityParameter
* Endpoint values/write now needs all (non implicit) WriteRequests within a ChannelGroup
* Fixed encoding issue in csv adapter
* Add feature for interactive search filter creation
* Using case sensitive equals when loading ProjectDomain
* Clearing where condition in case ODSQuery#fetch is called twice with another filter
* Fix TemplateAttribute default value when used with date or sequence values
* Make EnumRegistry use ConcurrentHashMap
* Fixed data-read endpoint when reading independent channel
* Unit conversion support for data-read endpoint
* Added translation if template component is added
* Added multiple scrollbars throughout the administration module
* Implemented sensor management in openMDM administration
* Removed unnecessary statements from ExportTask to improve export performance
* Integrate mdm-table-export into search result table
* Added enumeration administration
* Localize ASAM ODS enumerations in Value display of Context and in Navigator Nodes of node provider
* Added GUI support for deleting entities of main hierarchy (Project, Pool, ..., Measurement)
* Moved ODSModelManager#getTimeZone to ModelManager
* Added role SpecialistAdmin
* Improve x/y viewer with multiple axes and zoom
* Fix search bugs and new suggestions handling
* Fixes for GenericNodeProvider "Show in Tree" issues
* and man smaller fixes

## Version 5.2.0

Bugfixes:
* Web-Client: Fixed NaN in Date Picker

## Version 5.2.0M5

### Features ###

* Selection dialog for connected datasources
* Support Unit and Quantity in web client
* Support for Tags
* Support for ParameterSets
* User guide available

### API changes ###
* service.xml schema changed: tag <service> has a new attribute 'sourceName'. Make sure to update your existing service.xml.
* The /query endpoint unintentionally returned the Id of the requested resultType as a column. This Id column will not longer be returned unless explicitly requested.
* Added new Entities MDMFile, TestFile, TestStepFile, MeaResultFile and DescriptiveFile.
* When creating/removing a Project in the Rest-API, the ProjectDomain is also created/removed.
* The values endpoint supports to retrieve the full flags with query parameter '?flags=full'.
* Additional smaller changes to REST and Java-API
* openMDM boolean flag is calculated based on ods valid bit

####Enumeration changes:

Enumeration constructor needs additional parameter sourceName:
org.eclipse.mdm.api.base.model.Enumeration.Enumeration(String, Class<E>, String)

EnumRegistry removed parameter name. The name is taken from the given enumeration.
org.eclipse.mdm.api.base.model.EnumRegistry.add(Enumeration<? extends EnumerationValue>)

Added parameter sourceName
org.eclipse.mdm.api.base.model.EnumRegistry.get(String, String)
org.eclipse.mdm.api.base.model.EnumRegistry.list(String)

Removed methods:
org.eclipse.mdm.api.dflt.model.EntityFactory.createCatalogAttribute(String, ValueType<?>, String, CatalogComponent)
org.eclipse.mdm.api.dflt.model.EntityFactory.createCatalogAttribute(String, ValueType<?>, String, CatalogComponent, Unit)
in favor of methods:
org.eclipse.mdm.api.dflt.model.EntityFactory.createCatalogAttribute(String, ValueType<?>, Enumeration<?>, CatalogComponent)
org.eclipse.mdm.api.dflt.model.EntityFactory.createCatalogAttribute(String, ValueType<?>, Enumeration<?>, CatalogComponent, Unit)

EnumResource takes additional parameter sourceName: "/environments/{sourceName}/enums{enumName}/"

### Changes ###
* [549846](https://bugs.eclipse.org/bugs/show_bug.cgi?id=549846) - [REST] Add DataType attribute to CatalogComponentAttribute response
* [574293](https://bugs.eclipse.org/bugs/show_bug.cgi?id=574293) - Add parameter to fail the import if specifed template does not exist
* [573745](https://bugs.eclipse.org/bugs/show_bug.cgi?id=573745) - Show ordered and measured values in search result list if values are different
* [573705](https://bugs.eclipse.org/bugs/show_bug.cgi?id=573705) - Add userguide document
* [573451](https://bugs.eclipse.org/bugs/show_bug.cgi?id=573451) - Add Unit and Quantity administration in web client
* [572513](https://bugs.eclipse.org/bugs/show_bug.cgi?id=572513) - Add support for MDMTags
* [572514](https://bugs.eclipse.org/bugs/show_bug.cgi?id=572514) - Implemented support for ParameterSets
* [577547](https://bugs.eclipse.org/bugs/show_bug.cgi?id=577547) - Add enpoints assigning and retrieve TemplateMeasurements for a TemplateTestStep
* [577548](https://bugs.eclipse.org/bugs/show_bug.cgi?id=577548) - Add support to assign Files to a TemplateAttribute
* [578740](https://bugs.eclipse.org/bugs/show_bug.cgi?id=578740) - Add selection dialog for connected datasources

### Bugzilla Bugs fixed ###
* [577083](https://bugs.eclipse.org/bugs/show_bug.cgi?id=577083) - ATFx-Adapter throws IndexOutOfBoundsException for ATFx-Files containing Quantities without the optional Unit
* [572836](https://bugs.eclipse.org/bugs/show_bug.cgi?id=572836) - Support for background indexing
* [574565](https://bugs.eclipse.org/bugs/show_bug.cgi?id=574565) - NPE in ContextService if some attributes are not set
* [574184](https://bugs.eclipse.org/bugs/show_bug.cgi?id=574184) - Fulltext search returns all tests if elasticsearch has no results
* [574291](https://bugs.eclipse.org/bugs/show_bug.cgi?id=574291) - Unit mapping does not work for ATFX import
* [573687](https://bugs.eclipse.org/bugs/show_bug.cgi?id=573687) - Elasticsearch only returns the first 10 results by default
* [573494](https://bugs.eclipse.org/bugs/show_bug.cgi?id=573494) - MDMEntityAccessException when deleting channel in REST api
* [573332](https://bugs.eclipse.org/bugs/show_bug.cgi?id=573332) - Several minor bugfixes concerning xy-chartviewer and export
* [573072](https://bugs.eclipse.org/bugs/show_bug.cgi?id=573072) - ATFX export fails if data contains Unit with * in name
* [572895](https://bugs.eclipse.org/bugs/show_bug.cgi?id=572895) - An error occurs when adding 2 channels to the shopping basket
* [572811](https://bugs.eclipse.org/bugs/show_bug.cgi?id=572811) - Open file descriptors after ATFX import
* [572628](https://bugs.eclipse.org/bugs/show_bug.cgi?id=572628) - Relation 'Unit' at Quantity should be optional
* [572456](https://bugs.eclipse.org/bugs/show_bug.cgi?id=572456) - Problems with Integer Fields in Search and Editing
* [572991](https://bugs.eclipse.org/bugs/show_bug.cgi?id=572991) - An error occurs when editing a context attribute of type DS_EXTERNALREFERENCE
* [572628](https://bugs.eclipse.org/bugs/show_bug.cgi?id=572628) - Relation 'Unit' at Quantity should be optional
* [577568](https://bugs.eclipse.org/bugs/show_bug.cgi?id=577568) - potential NPE in ConnectorService#reconnect


## Version 5.2.0M4

### Features ###

**Node Provider:**
The navigation structure of the client application can be adapted to the needs with one or more
configurations of the Node Provider.

**Documentation:**
This documentation was transferred to AsciiDoc, the sources are now part of the mdm Git repository: `org.eclipse.mdm/doc`

### API changes ###
org.eclipse.mdm.api.base:
* New method: o.e.m.a.b.BaseApplicationContext#getSourceName()
* New method: o.e.m.a.b.a.ModelManager#getMimeType(EntityType entityType)
* New method: o.e.m.a.b.m.WriteRequestBuilder#explicitExternal()
* New method: o.e.m.a.b.m.WriteRequestBuilder#rawLinearExternal(double offset, double factor)
* New method: o.e.m.a.b.m.WriteRequestBuilder#rawPolynomialExternal(double... coefficients)
* New method: o.e.m.a.b.m.WriteRequestBuilder#rawLinearCalibratedExternal(double offset, double factor, double calibration)
* New method: o.e.m.a.b.m.AnyTypeValuesBuilder#externalComponent(ScalarType scalarType, ExternalComponent externalComponent)
* New method: o.e.m.a.b.m.AnyTypeValuesBuilder#externalComponents(ScalarType scalarType, Short globalFlag, ExternalComponent externalComponent)
* New method: o.e.m.a.b.m.AnyTypeValuesBuilder#externalComponents(ScalarType scalarType, List<ExternalComponent> externalComponents)
* New method: o.e.m.a.b.m.AnyTypeValuesBuilder#externalComponents(ScalarType scalarType, Short globalFlag, List<ExternalComponent> externalComponents)
* New method: o.e.m.a.b.q.Filter#id(ContextState contextState, EntityType entityType, String id)
* New method: o.e.m.a.b.q.Filter#copy()
* New method: o.e.m.a.b.q.hasContext()
* New method: o.e.m.a.b.q.getContext()
* New method: o.e.m.a.b.q.Condition#getContextState()
* New method: o.e.m.a.b.s.SearchQuery#getFilterResults(List<Attribute> attributes, Filter filter, ContextState contextState)
* New method: o.e.m.a.b.s.getFilterResults(Class<? extends Entity> entityClass, List<Attribute> attributes, Filter filter, ContextState contextState)

* New Entity: o.e.m.a.b.m.MDMLocalization

* New Enum: o.e.m.a.b.s.ContextState

HTTP endpoints:

* The endpoints `/environments/{SOURCE_NAME}/{TYPE}/localizations` was deprecated, in favor of new endpoint `/environments/{SOURCE_NAME}/mdmlocalizations`.
* New endpoint for nodeprovider: `/nodeprovider`


### Changes ###
* [570435](https://bugs.eclipse.org/bugs/show_bug.cgi?id=570435) - Usability improvements in X-Y-Chartviewer
* [570654](https://bugs.eclipse.org/bugs/show_bug.cgi?id=570654) - Exported ATFx files should use DT_LONGLONG for FlagsStartOffset
* [569462](https://bugs.eclipse.org/bugs/show_bug.cgi?id=569462) - Quickviewer and ChartViewer should take flags into account
* [544281](https://bugs.eclipse.org/bugs/show_bug.cgi?id=544281) - Use openJDK 8 for Jenkins builds
* [570877](https://bugs.eclipse.org/bugs/show_bug.cgi?id=570877) - Nodeprovider: Support group by year/month/day for attributes of type date
* [570273](https://bugs.eclipse.org/bugs/show_bug.cgi?id=570273) - Support addtional operators in attribute search
* [566724](https://bugs.eclipse.org/bugs/show_bug.cgi?id=566724) - Provide AsciiDoc Documentation Files and Build
* [570652](https://bugs.eclipse.org/bugs/show_bug.cgi?id=570652) - Update to openATFx 0.8.10 to support NAN in ATFx files
* [568979](https://bugs.eclipse.org/bugs/show_bug.cgi?id=568979) - Add getSourceName() to BaseApplicationContext
* [571199](https://bugs.eclipse.org/bugs/show_bug.cgi?id=571199) - Localization of components and attributes in context data
* [561943](https://bugs.eclipse.org/bugs/show_bug.cgi?id=561943) - Nodeprovider: Features Prio 1
* [570436](https://bugs.eclipse.org/bugs/show_bug.cgi?id=570436) - Possibility to sync between navigator und search
* [571282](https://bugs.eclipse.org/bugs/show_bug.cgi?id=571282) - Remove limitation to 1000 channels in X/Y-Chartviewer
* [571879](https://bugs.eclipse.org/bugs/show_bug.cgi?id=571879) - Usability improvements in search component


### Bugzilla Bugs fixed ###
* [571599](https://bugs.eclipse.org/bugs/show_bug.cgi?id=571599) - Cannot create preferences in Administration component
* [570922](https://bugs.eclipse.org/bugs/show_bug.cgi?id=570922) - Error when leaving attribute search field in Advanced Search
* [570720](https://bugs.eclipse.org/bugs/show_bug.cgi?id=570720) - Avoid calling ReadRequestHandler#getODSColumns() when STORAGE_PRESERVE_EXTCOMPS  is used in import
* [568947](https://bugs.eclipse.org/bugs/show_bug.cgi?id=568947) - Remove readValues Request for loading axistype and independent flag in chart-viewer-data.service
* [570924](https://bugs.eclipse.org/bugs/show_bug.cgi?id=570924) - ATFx exports with multiple files produce invalid zip files
* [570923](https://bugs.eclipse.org/bugs/show_bug.cgi?id=570923) - Error when saving tests with a TplTestStep which does not have a TplTestSequence
* [569591](https://bugs.eclipse.org/bugs/show_bug.cgi?id=569591) - Exception if downloading file with empty/invalid mimetype
* [570756](https://bugs.eclipse.org/bugs/show_bug.cgi?id=570756) - ImportService does not release file handles correctly
* [571599](https://bugs.eclipse.org/bugs/show_bug.cgi?id=571599) - Cannot create preferences in Administration component
* [571848](https://bugs.eclipse.org/bugs/show_bug.cgi?id=571848) - ATFx export incomplete, if external components are exported
* [571861](https://bugs.eclipse.org/bugs/show_bug.cgi?id=571861) - Avoid unnecessary updates of existing elements during import
* [572480](https://bugs.eclipse.org/bugs/show_bug.cgi?id=572480) - Filename for atfx-files

## Version 5.2.0M3

### API changes ###
org.eclipse.mdm.api.base:

* New method: o.e.m.a.b.BaseEntityManager#loadAll(Class<T>, Collection<String>);
* New method: o.e.m.a.b.q.Filter#namesOnly(EntityType, Collection<String>);

* New method: o.e.m.a.b.m.AnyTypeValuesBuilder#externalComponent(ScalarType, ExternalComponent)
* New method: o.e.m.a.b.m.AnyTypeValuesBuilder#externalComponents(ScalarType, Short, ExternalComponent)
* New method: o.e.m.a.b.m.AnyTypeValuesBuilder#externalComponents(ScalarType, List<ExternalComponent>)
* New method: o.e.m.a.b.m.AnyTypeValuesBuilder#externalComponents(ScalarType, Short, List<ExternalComponent>)
* New method: o.e.m.a.b.m.ExternalComponent#ExternalComponent(...)
* New method: o.e.m.a.b.m.ExternalComponent#get...()
* New method: o.e.m.a.b.m.ExternalComponent#set...()
* New method: o.e.m.a.b.m.WriteRequest#getGlobalFlag()
* New method: o.e.m.a.b.m.WriteRequest#setGlobalFlag(boolean)
* New method: o.e.m.a.b.m.MeasuredValues#hasExternalComponents()
* New method: o.e.m.a.b.m.MeasuredValues#getExternalComponents()

* New Enum: o.e.m.a.b.m.ValuesMode#STORAGE_PRESERVE_EXTCOMPS

* New Entity: o.e.m.a.b.m.ExternalComponentEntity

* New method: o.e.m.a.b.m.ScalarType#createMeasuredValues(String, String, SequenceRepresentation, double[], boolean, AxisType, Short, List<ExternalComponent>)
* New method: o.e.m.a.b.m.SequenceRepresentation#getNonExternalCounterpart()
* New method: o.e.m.a.b.m.Value#extractWithDefault(T defaultValue)


### Changes ###

* [568631](https://bugs.eclipse.org/bugs/show_bug.cgi?id=568631) - Show multiple Channels in Quickviewer data table
* [568630](https://bugs.eclipse.org/bugs/show_bug.cgi?id=568630) - Show Status of Test, TestStep and Measurement in web client
* [568159](https://bugs.eclipse.org/bugs/show_bug.cgi?id=568159) - Add "Copy link" to context menu of navigator
* [568110](https://bugs.eclipse.org/bugs/show_bug.cgi?id=568110) - Add support for refreshing the navigator tree
* [567589](https://bugs.eclipse.org/bugs/show_bug.cgi?id=567589) - ATFX Import: Avoid using unsupported operation of openatfx
* [567588](https://bugs.eclipse.org/bugs/show_bug.cgi?id=567588) - Provide a meaningful exception, if the underlying adapter lost its connection.
* [567587](https://bugs.eclipse.org/bugs/show_bug.cgi?id=567587) - Implement append functionality for channels
* [566555](https://bugs.eclipse.org/bugs/show_bug.cgi?id=566555) - Allow custom EntityConfigRepositoryLoader via ODSContextFactory
* [566598](https://bugs.eclipse.org/bugs/show_bug.cgi?id=566598) - Make PARAM_FOR_USER constant public
* [565431](https://bugs.eclipse.org/bugs/show_bug.cgi?id=565431) - Add support for handling external components import/export


### Bugzilla Bugs fixed ###

* [568567](https://bugs.eclipse.org/bugs/show_bug.cgi?id=568567) - ODSQuery returns invalid results if Channel and ChannelGroup
* [565036](https://bugs.eclipse.org/bugs/show_bug.cgi?id=565036) - ScalarType STRING is not displayed in chartviewer data tab
* [564539](https://bugs.eclipse.org/bugs/show_bug.cgi?id=564539) - [REST] If creating a channelgroup, the endpoint throws a NoSuchMethodException, because NumberOfValues parameter is not matched
* [564996](https://bugs.eclipse.org/bugs/show_bug.cgi?id=564996) - xy chartviewer does not consider preview checkbox correctly
* [564995](https://bugs.eclipse.org/bugs/show_bug.cgi?id=564995) - Overlay messages stop working, if one message was dismissed by an user
* [559749](https://bugs.eclipse.org/bugs/show_bug.cgi?id=559749) - Preference administration faulty
* [565287](https://bugs.eclipse.org/bugs/show_bug.cgi?id=565287) - Attached file with UTF-8 name support


## Version 5.2.0M2

The old repositories are not used any more, they will be archived soon. Do not use!

### New Repository Structure
All code from the repositories
- org.eclipse.mdm.api.base
- org.eclipse.mdm.api.default
- org.eclipse.mdm.api.odsadapter
- org.eclipse.mdm.nucleus

moved to org.eclipse.mdm, including the history, the branches master, dev release_candidate and existing committer feature branches.
The following tags were created: 5.0.0, 5.1.0, 5.2.0M1.
If you need an older tag file a Bugzilla Bug.

The structure in the new repository:

* `root`: build scripts, documentation and legal documentation
* `api`
  * `base`: code from former repos org.eclipse.api.base and org.eclipse.api.default, package names are unchanged
  * `odsadapter`: code from repo org.eclipse.api.odsadapter
  * `atfxadapter`: code from repo org.eclipse.mdm.nucleus/org.eclipse.mdm.api.atfxadapter
* `nucleus`
  * code from repo org.eclipse.mdm.nucleus except for the atfxadapter component
* `gradle`: the gradle wrapper
* `doc`: documentation from org.eclipse.mdm.nucleus/doc
* `tools`: new folder for common tooling, e.g. formatter, repository migration file

### API changes ###

None

### Bugzilla Bugs fixed ###

* [560684](https://bugs.eclipse.org/bugs/show_bug.cgi?id=560684) - Restructuring of mdmbl repos


## Version 5.2.0M1

New Features:
* Upload/download file attachments to FileAttachables and context attributes
* Show/edit context data based on MDM templates
* ATFxAdapter supports model mapping via ExtSystem
* XY-Chartviewer to view measurement data
* Introduce user roles on the webclient

### Breaking changes ###

#### Changes to MDMRealm ####
Up to version 5.1.0 all Users had to be assigned the group `MDM` to use openMDM.
Since 5.2.0M1 users need one of the following groups: `Admin`, `DescriptiveDataAuthor`, `Guest`.
See `org.eclipse.mdm.nuclues/readme.md#Configure LoginModule` for more information.

#### Database schema changes ####
In this release openMDM 5 database schema was extended.
If you have an existing installation make sure to apply the provided update script to your database.
The script can be found in `schema/update/{DATABASE}/5.2.0M1_update.sql`.


### API changes ###
REST-API:
* Tests, teststeps and measurements have new subresource files to upload, download, and delete files.
* Teststep and measurement contexts have new subresource to upload, download, and delete files at component attributes.
* New user resource to get information about the currently logged in user and its roles.

org.eclipse.mdm.api.base:
* New method: o.e.m.a.b.f.FileService#uploadSequential(Entity, Collection<FileLink>, ProgressListener)
* New method: o.e.m.a.b.f.FileService#uploadParallel(Entity, Collection<FileLink>, ProgressListener)
* New method: o.e.m.a.b.f.FileService#delete(Entity, Collection<FileLink>)
* New method: o.e.m.a.b.f.FileService#delete(Entity, FileLink)
* New method: o.e.m.a.b.f.FileLink#newLocal(InputStream, String, long, MimeType, String)
* New method: o.e.m.a.b.m.FileLink#getLocalStream()
* New method: o.e.m.a.b.m.FileLink#setLocalStream(InputStream)
* Removed method: o.e.m.a.b.m.FileLink#getLocalPath()
* Removed method: o.e.m.a.b.m.FileLink#setLocalPath(Path)

org.eclipse.mdm.api.default
* New method: o.e.m.a.d.m.EntityFactory#createExtSystem(String)
* New method: o.e.m.a.d.m.EntityFactory#createExtSystemAttribute(String, ExtSystem)
* New method: o.e.m.a.d.m.EntityFactory#createMDMAttribute(String, ExtSystemAttribute)
* New method: o.e.m.a.d.m.createTestStepWithOutContextRoots(Test, TemplateTestStep, Classification)
* New method: o.e.m.a.d.m.createTestStepWithOutContextRoots(Test, TemplateTestStep)
* New method: o.e.m.a.d.m.createTestStepWithOutContextRoots(Test, Status, TemplateTestStep)
* New entity: o.e.m.a.d.m.ExtSystem
* New entity: o.e.m.a.d.m.ExtSystemAttribute
* New entity: o.e.m.a.d.m.MDMAttribute
* New entity: o.e.m.a.d.m.SystemParameter
* New entity: o.e.m.a.d.m.UserParameter

### Changes ###

* [552399](https://bugs.eclipse.org/bugs/show_bug.cgi?id=552399) - Upload/download file attachments in webclient
* [552400](https://bugs.eclipse.org/bugs/show_bug.cgi?id=552400) - Show/edit context data based on MDM templates
* [552401](https://bugs.eclipse.org/bugs/show_bug.cgi?id=552401) - Model mapping in atfxadapter with ExtSystem
* [552402](https://bugs.eclipse.org/bugs/show_bug.cgi?id=552402) - Simple XY-Chartviewer
* [552403](https://bugs.eclipse.org/bugs/show_bug.cgi?id=552403) - Introduce user roles on the webclient
* [562384](https://bugs.eclipse.org/bugs/show_bug.cgi?id=562384) - Freetextindexer should only index once in clustered deployments

## Version 5.1.0 ##

### Bugzilla Bugs fixed ###

* [561163](https://bugs.eclipse.org/bugs/show_bug.cgi?id=561163) - Possible NPE in OdsEntityManager#loadTemplate()
* [553867](https://bugs.eclipse.org/bugs/show_bug.cgi?id=553867) - Upload shopping basket should use xml format
* [553699](https://bugs.eclipse.org/bugs/show_bug.cgi?id=553699) - Provide a user friendly error message if unsupported business objects are exported to atfx.
* [560003](https://bugs.eclipse.org/bugs/show_bug.cgi?id=560003) - Relations in Response not shown correctly

## Version 5.1.0RC1 ##

New Features:
* Support for Elasticsearch Version 7 or later

### Changes ###

* [520297](https://bugs.eclipse.org/bugs/show_bug.cgi?id=520297) - Fulltextsearch should support Elastic Search Version 7

## Version 5.1.0M8, 2020/02/03 ##

New Features:
* Add Quantity and Unit in Detail-View in WebClient
* Support for Status using Classification

### API changes ###

* REST-API has a new endpoints ProjectDomain, Status and Classification
* Responses now contain relation information in property "relation"
* New method: o.e.m.a.d.EntityManager#loadTemplate(Test)
* New method: o.e.m.a.d.EntityManager#loadTemplate(TestStep)
* New method: o.e.m.a.d.m.EntityFactory#createClassification(Domain, ProjectDomain, Status)
* New method: o.e.m.a.d.m.EntityFactory#createTest(String, Pool, TemplateTest, Classification, boolean)
* New method: o.e.m.a.d.m.EnttiyFactory#createTestStep(Test, TemplateTestStep, Classification)
* New method: o.e.m.a.d.m.EnttiyFactory#createTestStep(String, Test, Classification)
* New method: o.e.m.a.d.m.EntityFactory#createTestStep(String, Test, TemplateTestStep, Classification)
* Renamed classes in org.eclipse.mdm.nucleus.api.copy
* New method: o.e.m.a.b.q.Query#limit(int limit)

### Changes ###

* [553266](https://bugs.eclipse.org/bugs/show_bug.cgi?id=553266) - Contributions of Peak Solution (2019/11)
* [546639](https://bugs.eclipse.org/bugs/show_bug.cgi?id=546639) - Use openMDM 5 API to create Classifications
* [552484](https://bugs.eclipse.org/bugs/show_bug.cgi?id=552484) - Add parameter to specify the hostname in addition to the interface name for CORBAFileServer
* [552505](https://bugs.eclipse.org/bugs/show_bug.cgi?id=552505) - Webclient: All URLs routed by Angular have to fallback to index.html
* [553162](https://bugs.eclipse.org/bugs/show_bug.cgi?id=553162) - Webclient: Add Quantity and Unit to DetailView
* [553163](https://bugs.eclipse.org/bugs/show_bug.cgi?id=553163) - Implement a session check / reconnect for Freetextindexer
* [553164](https://bugs.eclipse.org/bugs/show_bug.cgi?id=553164) - Improve template resolving in apicopy
* [550002](https://bugs.eclipse.org/bugs/show_bug.cgi?id=550002) - Add relation information to REST-API
* [549844](https://bugs.eclipse.org/bugs/show_bug.cgi?id=549844) - [REST] Add unit info on /channels response
* [544850](https://bugs.eclipse.org/bugs/show_bug.cgi?id=544850) - Allow to limit number of search results

### Bugzilla Bugs fixed ###

* [553369](https://bugs.eclipse.org/bugs/show_bug.cgi?id=553369) - ATFX Import fails if multiple version of a Quantity exist
* [559366](https://bugs.eclipse.org/bugs/show_bug.cgi?id=559366) - [REST] Swagger Codegen leads to an error when using the provided openAPI files
* [553266](https://bugs.eclipse.org/bugs/show_bug.cgi?id=553266)) - Contributions of Peak Solution (2019/11)
  - ATFX export now correctly includes Context Data
  - Fixed EntityService#satisfiesParameters
  - Fixed memory leak when explicitly closing an context in ConnectorService
  - Fixed: MeaQuantity name is set to DefMQName instead actual value on import/export
  - Do not log ServiceConfiguration on connection failure.
  - Remove odsadapter dependency from apicopy
  - Fixed shopping basket context menu

## Version 5.1.0M7, 2019/11/08 ##

New Features:
* Support for reading and writing measurement data.
* Support for reading preview measurement values.
* Measurement data can be transferred as Protocol Buffers or JSON

### API changes ###

* REST-API has a new endpoints read (/mdm/environments/{SOURCENAME}/values/read) and write (/mdm/environments/{SOURCENAME}/values/write) measurement values.
* REST-API has a new endpoint to request preview values (/mdm/environments/{SOURCENAME}/values/preview)
* o.e.m.a.b.m.ReadRequestBuilder now additionally has methods ReadRequestBuilder.values(int requestSize) and ReadRequestBuilder.values(int startIndex, int requestSize) as terminal operations.

### Changes ###

* [548162](https://bugs.eclipse.org/bugs/show_bug.cgi?id=548162) - Add support for reading/writing measurement data over REST API

### Bugzilla Bugs fixed ###

* [547141](https://bugs.eclipse.org/bugs/show_bug.cgi?id=547141) - ReadRequestBuilder does not allow to create ReadRequests with startIndex or requestSize != 0
* [547115](https://bugs.eclipse.org/bugs/show_bug.cgi?id=547115) - ClassCastException in ReadRequestHandler#getODSColumns

## Version 5.1.0M6, 2019/10/18 ##

New Features:
* Generate a openAPI definition file of the openMDM REST-API at build time.
* Integrated Swagger UI into deployment for visualizing the API.


### API changes ###

* no

### Changes ###

* [537293](https://bugs.eclipse.org/bugs/show_bug.cgi?id=537293) - Use swagger for rest api docu

### Bugzilla Bugs fixed ###

* [552188](https://bugs.eclipse.org/bugs/show_bug.cgi?id=552188) - Context menu in shopping basket is not displayed correctly
* [551950](https://bugs.eclipse.org/bugs/show_bug.cgi?id=551950) - FreeTextIndexer cannot be deactivated

## Version 5.1.0M5, 2019/08/27 ##

New Features:
* Support for reading FILE_LINK and FILE_LINK_SEQUENCE with REST-API


### API changes ###

* Added REST endpoints test/files/{remotePath}, teststep/files/{remotePath} and measurement/files/{remotePath}
* Attributes with datatype `FILE_LINK`/`FILE_LINK_SEQUENCE` will return JSON objects with properties `remotePath`, `mimeType` and `description` as value.

### Changes ###

* [546774](https://bugs.eclipse.org/bugs/show_bug.cgi?id=546774) - Add support for reading FILE_LINK_SEQUENCE attributes

### Bugzilla Bugs fixed ###

-

## Version 5.1.0M4, 2019/08/09 ##

New Features:
 * Eclipse Glassfish 5 Support


### API changes ###

* no

### Changes ###

*  [548241](https://bugs.eclipse.org/bugs/show_bug.cgi?id=548241) - Test nucleus with Eclipse Glassfish 5.1

### Bugzilla Bugs fixed ###

* [548347](https://bugs.eclipse.org/bugs/show_bug.cgi?id=548347) - Advanced search with date attribute fails if time portion is omitted in input field
* [547115](https://bugs.eclipse.org/bugs/show_bug.cgi?id=547115) - ClassCastException in ReadRequestHandler#getODSColumns

## Version 5.1.0M3, 2019/04/23 ##

New Features:
 * Angular 7 support, updated from Angular 2
 * I18N support for web frontend, languages available: English, German.
    * For adding a new language see: org.eclipse.mdm.nucleus/org.eclipse.mdm.application/src/main/webapp/README_I18N.md
    * Files with translations: org.eclipse.mdm.nucleus/org.eclipse.mdm.application/src/main/webapp/src/assets/i18n/\*.json


### API changes ###

* no

### Changes ###

*  [544530](https://bugs.eclipse.org/bugs/show_bug.cgi?id=544530) - Angular 7 Upgrade and I18N for Webfrontend

### Bugzilla Bugs fixed ###

* [544621](https://bugs.eclipse.org/bugs/show_bug.cgi?id=544621) - Mixed line endings (LF and CRLF) and other formatting issues
* [545400](https://bugs.eclipse.org/bugs/show_bug.cgi?id=545400) - Apostroph ' (%27) in urls with filter parameters is blocked by proxy servers
* [545591](https://bugs.eclipse.org/bugs/show_bug.cgi?id=545591) - OdsSearchService creates invalid ODS Query
* [544562](https://bugs.eclipse.org/bugs/show_bug.cgi?id=544562) - Nucleus - navigator: Dropdown menu sources
* [543525](https://bugs.eclipse.org/bugs/show_bug.cgi?id=543525) - Dependency Cleanup


## Version 5.1.0M2, 2019/02/28 ##

New Features:
 * Read root configuration directory from system property ("org.eclipse.mdm.configPath")
 * ATFX import/export
 * Support added for the Entity Role in the openMDM 5 Java API: load and persist the many-to-many relation between User and Role.
 * The business objects Project, Pool, Test, Teststep, Measurement are editable via the REST API. It is possible create, update and delete the listed business objects. Furthermore it is possible to edit the context data for Teststep and Measurement. CRUD operations for measured data are not part of this ticket.
 * Property lookups for parameters values in service.xml: The service.xml contains all information necessary for the Connector-Service to connect to the available datasources/adapter instances. Since this information includes secret information like passwords, it is possible to provide lookups, which gives you the possibility to specify tokens as references to properties defined elsewhere.

### API changes ###

* All Repos: added Source Jar to build
* o.e.m.api.base (impl): TestStep#loadContexts returns cached values if present
* o.e.m.api.base (impl): Added IndependentBuilder#independent(boolean)
* o.e.m.api.base (impl): Added ReadRequest#getValuesMode()/setValuesMode(ValuesMode)
* o.e.m.api.base (impl): Added ReadRequestBuilder#valuesMode(ValuesMode)
* o.e.m.api.base (impl): Added WriteRequest#setIndependent(boolean)
* o.e.m.api.base (impl): Extended MeasuredValues with SequenceRepresentation, Generation Parameters, Independent Flag and AxisType
* o.e.m.api.base (API): Added Parameters to ScalarType#createmeasuredValues()
* o.e.m.api.default (impl): EntityFactory#createTestStep(Test, TemplateTestStep) is now public
* o.e.m.api.default (impl): Added EntityFactory#createTest(String, Pool)
* o.e.m.api.odsadapter: Added possibility to provide EntityConfigRepository OdsModelManager
* o.e.m.api.odsadapter: Added SequenceRepresentation etc. for LocalColumns
* o.e.m.api.default (impl): EntityFactory#createContextRoot(List<Measurement>, TemplateRoot)
* o.e.m.api.base: added o.e.m.a.b.BaseEntityManager.loadRelatedEntities
* o.e.m.api.base: added o.e.m.a.b.a.Core.getNtoMStore
* org.eclipse.mdm.api.default: added o.e.m.a.d.m.EntityFactory.createRole
* org.eclipse.mdm.api.default:added Entity o.e.m.a.d.m.Role

### Changes ###

* [540179](https://bugs.eclipse.org/bugs/show_bug.cgi?id=540179) - [[REQU-111]](https://openmdm.atlassian.net/browse/REQU-111) - Read root configuration directory from system property
* [540180](https://bugs.eclipse.org/bugs/show_bug.cgi?id=540180) -[[REQU-112]](https://openmdm.atlassian.net/browse/REQU-112) - ATFX import/export
* [545014](https://bugs.eclipse.org/bugs/show_bug.cgi?id=545014) - [[REQU-117]](https://openmdm.atlassian.net/browse/REQU-117) - CRUD operations for main business objects
* [542769](https://bugs.eclipse.org/bugs/show_bug.cgi?id=542769) -[[REQU-116]](https://openmdm.atlassian.net/browse/REQU-116) -  Entity Role
*  [544526](https://bugs.eclipse.org/bugs/show_bug.cgi?id=544526) - Property lookups for parameters values in service.xml

### Bugzilla Bugs fixed ###

*  [543675](https://bugs.eclipse.org/bugs/show_bug.cgi?id=543675) - ODSContextFactory leaks ORB instances
* [543440](https://bugs.eclipse.org/bugs/show_bug.cgi?id=543440) - Configuration: Nodeprovider

## Milestone 5.1.0M1, 2019/01/11 ##

### Changes ###

 * Support for Status was extended such that it can be attached to a Measurement / TestStep / Test class. Additional entities ProjectDomain Domain and Classification were added to the org.eclipse.mdm.api.default module
 * New system property "org.eclipse.mdm.api.odsadapter.filetransfer.interfaceName" to set a specific network interface name to be used


### Bugzilla Bugs fixed ###

 * [540889](https://bugs.eclipse.org/bugs/show_bug.cgi?id=540889)	- Contribution for 5.1.0M1 bug/feature collection
 * [529729](https://bugs.eclipse.org/bugs/show_bug.cgi?id=529729) - Status and MDMTag cannot be set on a test/testStep
 * [539983](https://bugs.eclipse.org/bugs/show_bug.cgi?id=539983) -	Corba File Server does not work on windows because of a path format problem
 * [539984](https://bugs.eclipse.org/bugs/show_bug.cgi?id=539984) -	Corba File Server always uses the first network interface
 * [541606](https://bugs.eclipse.org/bugs/show_bug.cgi?id=541606) - InstallationGuide improvements
 * [542519](https://bugs.eclipse.org/bugs/show_bug.cgi?id=542519) - 	Nullpointer exception in InsertStatement
 * [542658](https://bugs.eclipse.org/bugs/show_bug.cgi?id=542658)	-	Nucleus markdown file - error in json "specifications"
 * [542656](https://bugs.eclipse.org/bugs/show_bug.cgi?id=542656)	-	Nucleus markdown file - wrong documentation of valuelist values interface
 * [542094](https://bugs.eclipse.org/bugs/show_bug.cgi?id=542094) - NotificationManager throws IllegalArgumentException: Incompatible value type

## Release 5.0.0, 2018/11/07 ##

[Release Review and Graduation Review](https://projects.eclipse.org/projects/automotive.mdmbl/releases/5.0.0) of the mdmbl Eclipse project succeeded.


## Version 5.0.0M5, 2018/10/30 ##

### Changes ###

 * License switch from Eclipse Public License 1.0 to Eclipse Public License 2.0.

### Bugzilla Bugs fixed ###

 * [540226](https://bugs.eclipse.org/bugs/show_bug.cgi?id=540226)   -  Failed EJB lookup if using catcomps endpoint
 * [539716](https://bugs.eclipse.org/bugs/show_bug.cgi?id=539716)   -  License switch EPL-1.0 to EPL-2.0


## Version 5.0.0M4, 2018/09/26 ##

This code brings along a change with the realm configuration. You can configure your realm now in a standardized way.  In the [readme.md](http://git.eclipse.org/c/mdmbl/org.eclipse.mdm.nucleus.git/tree/README.md) in the org.eclipse.mdm.nucleus project is described how to setup and configure a file realm for local installations. You can also configure your LDAP, AD or others.
See the [glassfish documentation](https://javaee.github.io/glassfish/doc/4.0/security-guide.pdf)

Note: The component "org.eclipse.mdm.realms" is not used any longer.

### Changes ###

* [526883](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526883) - [[REQU-109]](https://openmdm.atlassian.net/browse/REQU-101) - Integrate external distribution "Authentification and Authorization"


### Bugzilla Bugs fixed ###

 * [535381](https://bugs.eclipse.org/bugs/show_bug.cgi?id=535381)   -       Delegated roles and rights with backend connectors

## Version 5.0.0M3, 2018/09/10 ##

In this milestone new REST APIs were added for editing MDM business objects.
The list of all REST APIs see the [readme.md](http://git.eclipse.org/c/mdmbl/org.eclipse.mdm.nucleus.git/tree/README.md) in the org.eclipse.mdm.nucleus project.

### API changes ###

* no

### Changes ###

* [526883](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526883) - [[REQU-101]](https://openmdm.atlassian.net/browse/REQU-101) - The MDM business objects should be editable via openMDM5 RESTful API (CRUD operations)
* [REQU-102](https://openmdm.atlassian.net/browse/REQU-102) - Deploy data model via RESTful API
* [REQU-110](https://openmdm.atlassian.net/browse/REQU-110) - Integrate external distribtion "CRUD Operations for administrative objects"

### Bugzilla Bugs fixed ###

* no

## Version 5.0.0M2, 2018/07/23 ##

### API changes ###

 * no

### Changes ###

* [REQU-108](https://openmdm.atlassian.net/browse/REQU-108) - Bug Fixes and enhancements

### Bugzilla Bugs fixed ###

  * [534866](https://bugs.eclipse.org/bugs/show_bug.cgi?id=534866)	- Results incorrect if attribute search is combined with full text search yielding no results
  * [535606](https://bugs.eclipse.org/bugs/show_bug.cgi?id=535606)	- Attribute search is not case insensitive
  * [536840](https://bugs.eclipse.org/bugs/show_bug.cgi?id=536840)	- Avoid function calls from html template for translation
  * [530512](https://bugs.eclipse.org/bugs/show_bug.cgi?id=530512)	- Missing Information after change tabs
  * [526163](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526163)	- Resolve unmet dependency error cause by ng2-dropdown-multiselect
  * [532425](https://bugs.eclipse.org/bugs/show_bug.cgi?id=532425)	- Update and rename ng2-bootstrap to ngx-bootstrap
  * [536229](https://bugs.eclipse.org/bugs/show_bug.cgi?id=536229)	- Provide a readable error message, if preference service throws an error




## Version 5.0.0M1, 2018/05/18 ##

Note: the system of version numbers has changed!

### API changes ###

 * no

### Changes ###

* [REQU-106](https://openmdm.atlassian.net/browse/REQU-106) - Provide legal documentation, IP Checks, end user content
* [REQU-107](https://openmdm.atlassian.net/browse/REQU-107) - Notification Service Implementation vis OMG Specs

### Bugzilla Bugs fixed ###

* [528033](https://bugs.eclipse.org/bugs/show_bug.cgi?id=528033)	- Web client does not quote entity IDs in filter strings
* [532154](https://bugs.eclipse.org/bugs/show_bug.cgi?id=532154)	- Update libraries for IP check
* [532167](https://bugs.eclipse.org/bugs/show_bug.cgi?id=532167)	- logging
* [532343](https://bugs.eclipse.org/bugs/show_bug.cgi?id=532343)	- Configure Logging
* [532165](https://bugs.eclipse.org/bugs/show_bug.cgi?id=532165)	- Java lib
* [532170](https://bugs.eclipse.org/bugs/show_bug.cgi?id=532170)	- freetext.notificationType
* [534643](https://bugs.eclipse.org/bugs/show_bug.cgi?id=534643)	- search-datepicker does not recognize model changes via input textbox

## Version V0.10, 2018/02/23 ##

### API changes ###

 * [529569](https://bugs.eclipse.org/bugs/show_bug.cgi?id=529569) - [[REQU-103]](https://openmdm.atlassian.net/browse/REQU-103) - Using Shopping Basket (Search results)

#####  An overview of the API changes made :

* Shopping basket:
  - Added methods:
    - String org.eclipse.mdm.api.base.BaseApplicationContext.getAdapterType()
    - Map<Entity, String> org.eclipse.mdm.api.base.BaseEntityManager.getLinks(Collection<Entity>)
  - file extensions:
When downloading the contents of a shopping basket, a file with extension `mdm` is generated. The file extension can be changed by adding a preference with key `shoppingbasket.fileextensions`. For example the used extension can be set to `mdm-xml` by setting the value to `{ "default": "mdm-xml" }`.

### Changes ###

### Bugzilla Bugs fixed ###

* [521880](https://bugs.eclipse.org/bugs/show_bug.cgi?id=521880)	- Component with empty FileLink can not be updated
* [526124](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526124)	- OpenMDM web (nucleus) broken as of 10-16-2017 because of version ranges/tracking versions
* [528261](https://bugs.eclipse.org/bugs/show_bug.cgi?id=528261)	- ODS EntityConfigRepository.find(EntityType) should throw IllegalArgumentException if requested entity type is not found	2018-01-11
* [525848](https://bugs.eclipse.org/bugs/show_bug.cgi?id=525848)	- ODSConverter cannot parse 17 character dates
* [529568](https://bugs.eclipse.org/bugs/show_bug.cgi?id=529568)	- Junit Tests have to run stand alone
* [525980](https://bugs.eclipse.org/bugs/show_bug.cgi?id=525980)	- Remove version range in org.eclipse.mdm.api.base/build.gradle
* [529629](https://bugs.eclipse.org/bugs/show_bug.cgi?id=529629)	- Query group and aggregate function are not working as expected
* [526141](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526141)	- Remove version range in org.eclipse.mdm.api.odsadapter/build.gradle
* [529867](https://bugs.eclipse.org/bugs/show_bug.cgi?id=529867)	- Jenkins builds fail since 12.01.17
* [526147](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526147)	- wrong logic in org.eclipse.mdm.api.default/src/main/java/org/eclipse/mdm/api/dflt/model/EntityFactory
* [529887](https://bugs.eclipse.org/bugs/show_bug.cgi?id=529887)	- FileUpload throws java.net.UnknownHostException
* [526260](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526260)	- Writing enumeration values
* [530511](https://bugs.eclipse.org/bugs/show_bug.cgi?id=530511)	- Missing progress bar
* [526763](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526763)	- Issues in dynamic enumeration handling
* [530775](https://bugs.eclipse.org/bugs/show_bug.cgi?id=530775)	- ODS adapter: getParameters exposes sensitive information
* [527673](https://bugs.eclipse.org/bugs/show_bug.cgi?id=527673)	- ValueType.UNKNOWN.createValue() throws NPE due to ValueType.type not being set
* [530791](https://bugs.eclipse.org/bugs/show_bug.cgi?id=530791)	- UpdateStatement is broken
* [528149](https://bugs.eclipse.org/bugs/show_bug.cgi?id=528149)	- Search tab in web UI should accept attribute values even if no suggestions are present
* [528193](https://bugs.eclipse.org/bugs/show_bug.cgi?id=528193)	- Autocomplete text box in search UI should accept custom values automatically
* [528260](https://bugs.eclipse.org/bugs/show_bug.cgi?id=528260)	- ValueType.UNKNOWN should have Object rather than Void as representation type



## Version V0.9, 2017/11/24 ##

### API changes ###

* [525536](https://bugs.eclipse.org/bugs/show_bug.cgi?id=525536) - [[REQU-50]](https://openmdm.atlassian.net/browse/REQU-50) - Modelling of relations
* [526880](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526880) -  [[REQU-65]](https://openmdm.atlassian.net/browse/REQU-65) Separation of interfaces
* [526882](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526882) -  [[REQU-78]](https://openmdm.atlassian.net/browse/REQU-78) Packaging
* [522277](https://bugs.eclipse.org/bugs/show_bug.cgi?id=522277) -  [[REQU-80]](https://openmdm.atlassian.net/browse/REQU-80) Referencing

#####  An overview of the API changes made :

* service.xml:
 - Property `entityManagerFactoryClass` has to be changed to `org.eclipse.mdm.api.odsadapter.ODSContextFactory` for ODS Datasources.
 - Datasource specific parameters for `NotificationService` and the freetext search parameters are now supplied by service.xml not by global.properties.


* org.eclipse.mdm.api.base:
 - New entry class `BaseApplicationContextFactory` instead of `BaseEntityManagerFactory`.
 - Services can now be retrieved from `BaseApplicationContext` instead of `BaseEntityManager`.
 - `NotificationManagerFactory` was removed; `NotficationManager` was renamed to `NotificationService`.
 - Query package was split into query and search.
 - Creation of Query (`ModelManager#createQuery()`) was moved from `ModelManager` to new `QueryService` class.
 - Moved `ModelManager`, `EntityType`, `Core` and similar to subpackage `adapter`
 - `EntityStore` and `ChildrenStore` are now top-level classes in the subpackage `adapter`
 - Moved `FileService` to subpackage file
 - Added methods:
   - `ContextComponent#getContextRoot()`
   - `ContextSensor#getContextComponent()`
  - Introduced new (protected) method `BaseEntityFactory.createBaseEntity(Class, Core)` for creating instances of classes derived from `BaseEntity` using an already existing `Core` instance. Must be overridden in derived classes to ensure the instances can be created via the (usually package-private) Constructor(Core) of the BaseEntity-derived class. If the constructor of the class passed to `createBaseEntity`  is not accessible, the super class implementation is called to try if the constructor is accessible from there.
  - Introduced new (protected) method `BaseEntityFactory.getCore(BaseEntity)` to extract the Core object from a BaseEntity instance.
  - Modified the interface of BaseEntityManager: the get*Store methods have been defined as non final.
  With that, these methods can be overriden in the OdsAdpter and be used there to access the stores without resorting directly to the core.
  There also comments added in several places to improve the understandibility of the current implementation.


* org.eclipse.mdm.api.default:
 - Introduced `ApplicationContextFactory` and `ApplicationContext` which extend their base counterparts. Should be unnecessary when merging api.base and api.default repositories.


* org.eclipse.mdm.api.odsadapter:
 - Adapted to the changes from `api.base` and `api.default`


* org.eclipse.mdm.nucleus:
 - Adapted to the changes from `api.base` and `api.default`
 - `ConnectorService` manages `ApplicationContexts` instead of `EntityManagers`
 - Datasource specific parameters for `NotificationService` are now supplied by service.xml not by global.properties.


### Changes ###

* [522278](https://bugs.eclipse.org/bugs/show_bug.cgi?id=522278) -  [[REQU-98]](https://openmdm.atlassian.net/browse/REQU-98) Search Parameter Parser
* [526881](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526881) -  [[REQU-75]](https://openmdm.atlassian.net/browse/REQU-75) Specifications

### Bugzilla Bugs fixed ###

* [521880](https://bugs.eclipse.org/bugs/show_bug.cgi?id=521880) -  Component with empty FileLink can not be updatet
* [526124](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526124) -  OpenMDM web (nucleus) broken as of 10-16-2017 because of version ranges/t
* [526763](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526763) -  Issues in dynamic enumeration handling
* [525980](https://bugs.eclipse.org/bugs/show_bug.cgi?id=525980) -  Remove version range in org.eclipse.mdm.api.base/build.gradle
* [526260](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526260) -  Writing enumeration values
* [526141](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526141) -  Remove version range in org.eclipse.mdm.api.odsadapter/build.gradle
* [525848](https://bugs.eclipse.org/bugs/show_bug.cgi?id=525848) -  ODSConverter cannot parse 17 character dates
* [526147](https://bugs.eclipse.org/bugs/show_bug.cgi?id=526147) -  wrong logic in org.eclipse.mdm.api.default/.../EntityFactory

## Version V0.8, 2017/09/08 ##

### API changes ###

  * [REQU-62](https://openmdm.atlassian.net/browse/REQU-62) - Polyvalant variants
  * [REQU-49](https://openmdm.atlassian.net/browse/REQU-49) - Extensibility of Entity Classes
  * [REQU-79](https://openmdm.atlassian.net/browse/REQU-79) - Consistent relationships
  * [REQU-97](https://openmdm.atlassian.net/browse/REQU-97) - Change handling of enumerations

### Changes ###

* [REQU-73](https://openmdm.atlassian.net/browse/REQU-73) - Representation class
* [REQU-74](https://openmdm.atlassian.net/browse/REQU-74) - Empty Interface
* [REQU-77](https://openmdm.atlassian.net/browse/REQU-77) - Name convention
* [REQU-82](https://openmdm.atlassian.net/browse/REQU-82) - Return-Type
* [REQU-91](https://openmdm.atlassian.net/browse/REQU-91) - Context Information

### Bugzilla Bugs fixed ###

* [520291](https://bugs.eclipse.org/bugs/show_bug.cgi?id=520291) - 	Elastic Search :: ElasticSearch answered 400, error while performing some search
* [521011](https://bugs.eclipse.org/bugs/show_bug.cgi?id=521011) - 	EntityFactory.createContextSensor always throws an exception
* [520330](https://bugs.eclipse.org/bugs/show_bug.cgi?id=520330) - 	Improve development setup in Eclipse
* [518063](https://bugs.eclipse.org/bugs/show_bug.cgi?id=518063) - 	Nucleus: config-dir is missing in the build artefact mdm-web.zip
* [518124](https://bugs.eclipse.org/bugs/show_bug.cgi?id=518124) - 	Configure JIPP and Sonar for mdmbl projects
* [518444](https://bugs.eclipse.org/bugs/show_bug.cgi?id=518444) - 	Unify used gradle versions and update to latest stable
* [518825](https://bugs.eclipse.org/bugs/show_bug.cgi?id=518825) - 	Nucleus build: create a separate gradle task for npm build
* [519212](https://bugs.eclipse.org/bugs/show_bug.cgi?id=519212) - 	Enable production mode for client build
* [519993](https://bugs.eclipse.org/bugs/show_bug.cgi?id=519993) - 	Create Gradle composite build
* [519453](https://bugs.eclipse.org/bugs/show_bug.cgi?id=519453) - 	org.eclipse.mdm.openatfx build can't download dependency
* [519995](https://bugs.eclipse.org/bugs/show_bug.cgi?id=519995) - 	Setup Guide and avalon
* [520248](https://bugs.eclipse.org/bugs/show_bug.cgi?id=520248) - 	Build of org.eclipse.mdm.api.odsadapter only works with "gradle clean install"
* [517057](https://bugs.eclipse.org/bugs/show_bug.cgi?id=517057) - 	Add Repository Descriptions



## Version V0.7, 2017/07/21 ##

### API changes ###
  * [REQU-48](https://openmdm.atlassian.net/browse/REQU-48) - Type of Entity-IDs


### Changes ###
* [REQU-67](https://openmdm.atlassian.net/browse/REQU-67) - Final
* [REQU-92](https://openmdm.atlassian.net/browse/REQU-92) - Error Handling

### Bugzilla Bugs fixed ###
* [519448](https://bugs.eclipse.org/bugs/show_bug.cgi?id=519448) - Build of of freetextindexer in org.eclipse.mdm.nucleus fails
* [518062](https://bugs.eclipse.org/bugs/show_bug.cgi?id=518062) - ODSAdapter: Encoding issue when switching to UTF-8
* [518060](https://bugs.eclipse.org/bugs/show_bug.cgi?id=518060) - ODSAdapter - junit tests fail
* [515748](https://bugs.eclipse.org/bugs/show_bug.cgi?id=515748) - Unable to build org.eclipse.mdm.nucleus
* [518335](https://bugs.eclipse.org/bugs/show_bug.cgi?id=518335) - Set executable flag for gradlew in git repo


## Version V0.6, 2017/06/07
### Changes ###

  * [REQU-2](https://openmdm.atlassian.net/browse/REQU-2) - Display a tree view for navigation
  * [REQU-3](https://openmdm.atlassian.net/browse/REQU-3) - Display icons in the tree view
  * [REQU-4](https://openmdm.atlassian.net/browse/REQU-4) - Display different ODS data sources in the tree view
  * [REQU-5](https://openmdm.atlassian.net/browse/REQU-5) - Expand serveral nodes of the tree view simultaneously
  * [REQU-6](https://openmdm.atlassian.net/browse/REQU-6) - Display a scroll bar in the tree vie
  * [REQU-7](https://openmdm.atlassian.net/browse/REQU-7) - Web Client GUI Adjustment
  * [REQU-9](https://openmdm.atlassian.net/browse/REQU-9) - Display tabs on Detail view
  * [REQU-10](https://openmdm.atlassian.net/browse/REQU-10) - Update Detail View
  * [REQU-12](https://openmdm.atlassian.net/browse/REQU-12) - Select data source for attribute-based search
  * [REQU-13](https://openmdm.atlassian.net/browse/REQU-13) - Definition or selection of a search query
  * [REQU-14](https://openmdm.atlassian.net/browse/REQU-14) - Limit search to a certain result type
  * [REQU-15](https://openmdm.atlassian.net/browse/REQU-15) - Display attributes of the selected data source(s)
  * [REQU-16](https://openmdm.atlassian.net/browse/REQU-16) - Set search attribute values
  * [REQU-18](https://openmdm.atlassian.net/browse/REQU-18) - Select data source for fulltext search
  * [REQU-22](https://openmdm.atlassian.net/browse/REQU-22) - Create and store a view for search results
  * [REQU-23](https://openmdm.atlassian.net/browse/REQU-23) - Select a view to display search results
  * [REQU-24](https://openmdm.atlassian.net/browse/REQU-24) - Filter fulltext search results
  * [REQU-25](https://openmdm.atlassian.net/browse/REQU-25) - Display actions for search results
  * [REQU-27](https://openmdm.atlassian.net/browse/REQU-27) - Select data objects for shoppping basket
  * [REQU-28](https://openmdm.atlassian.net/browse/REQU-28) - Store a shopping basket
  * [REQU-29](https://openmdm.atlassian.net/browse/REQU-29) - Select a shopping basket
  * [REQU-30](https://openmdm.atlassian.net/browse/REQU-30) - Export shopping basket
  * [REQU-31](https://openmdm.atlassian.net/browse/REQU-31) - Load an exported shopping basket
  * [REQU-32](https://openmdm.atlassian.net/browse/REQU-32) - Display actions for shopping basket
  * [REQU-85](https://openmdm.atlassian.net/browse/REQU-85) - Seach type date
  * [REQU-86](https://openmdm.atlassian.net/browse/REQU-86) - Search across multiple data sources
  * [REQU-95](https://openmdm.atlassian.net/browse/REQU-95) - Backend configuration
