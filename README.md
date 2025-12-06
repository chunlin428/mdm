## Documentation

Before you start, please take a look at the [Getting Started Guide](https://gitlab.eclipse.org/eclipse/mdmbl/org.eclipse.mdm/-/tree/master/doc/GettingStartedGuide) which can also be found for the versions in the [Download area](https://projects.eclipse.org/projects/automotive.mdmbl/downloads) of the project and in the `docs` directory relative to this README.md

## Minimum requirements
Java 8:
* Oracle Java SE 8u201 and higher http://www.oracle.com/technetwork/java/javase/downloads/
* AdoptJDK: openJDK 8 with HotSpot: https://adoptopenjdk.net/

> This project uses a Gradle Wrapper

## Build dependencies
Before you can build and deploy the application, you have to checkout the following repository:
* `git clone https://gitlab.eclipse.org/eclipse/mdmbl/org.eclipse.mdm/`

Now you can use `gradlew install` in the `org.eclipse.mdm` directory, but you should configure the application first.

## Generate Documentation
To generate the current version of documentation the following Gradle tasks can be used

* `:doc:asciidoctor`
* `:doc:asciidoctorPdf`

For example, you can generate both with
`./gradlew :doc:asciidoctor :doc:asciidoctorPdf`

The generated files will be located in `doc/build/docs`

## Build, deploy and configure the application

1. Edit the `org.eclipse.mdm/nucleus/webclient/src/main/webapp/src/app/core/property.service.ts` and set the `data_host` variable according to your deployment
 (This property will be used to create the REST URLs to communicate with the backend).

2. Build the application with `gradlew install`.
 The command `gradlew install` executed at `org.eclipse.mdm` creates a ZIP archive named `build/distributions/openMDM_application-${version}.zip` at `org.eclipse.mdm/nucleus/build/distributions`
 The ZIP archive contains the backend `org.eclipse.mdm.nucleus-${version}.war` and the configuration files in the `configuration` directory

3. Check that the database for the preference service is running, otherwise start it with `asadmin start-database` as described in the Getting Started Guide.

4. Deploy the WAR file `org.eclipse.mdm.nucleus-${version}.war` on your application server. Make sure to deploy the WAR file with application name `org.eclipse.mdm.nucleus`, otherwise the `LoginRealmModule` is not able to lookup the `ConnectorService` EJB.
 Additionally in the following examples, we assume that the context root is also set to `org.eclipse.mdm.nucleus`.
 When deploying from command line you can use `asadmin deploy --name org.eclipse.mdm.nucleus /path/to/org.eclipse.mdm.nucleus-${version}.war`

5. Copy the content of the extracted `configuration` folder to `GLASSFISH_ROOT/glassfish/domains/domain1/config`.
 There is also a system property `org.eclipse.mdm.configPath`, which can be used to redefine the location of the folder to another location.

6. Edit the `org.eclipse.mdm.connector/service.xml` file to configure the data sources

7. Configure a `LoginModule` with name `MDMRealm` (See section **Configure LoginModule** for details)

8. Restart the application server

9. Visit the main page of the client to make sure everything works fine.
 The main page of the client should be available under `http://SERVER:PORT/{APPLICATIONROOT}` (eg: http://localhost:8080/org.eclipse.mdm.nucleus)

## Configure LoginModule

MDM 5 backend implements the delegated approach to roles and permissions, wherein the data sources (ASAM ODS server, PAK cloud)
themselves can implement their own security scheme (which they already have) and then delegates the appropriate
user data to the backends.

In the case of ASAM ODS servers this is done using a technical user and the `for_user` attribute on the established
connection, so it is a form of 'login on behalf'.

In the case of PAK Cloud this is done by passing the user name along with a http header `X-Remote-User` which is then
used by PAK Cloud to establish the users roles (from an internal database or an external authentication provider).

Before the user is 'logged in on behalf' he is authenticated by a `LoginModule` within the Glassfish application server.
There are different implementations available (e.g. LDAP, Certificate, JDBC, ...). To keep this guide simple, we will setup
a `FileRealm`, which stores the user information in a flat file.

The following command will create a `FileRealm` with name `MDMRealm` that stores the users in a file called `mdm-keyfile`
```
asadmin create-auth-realm --classname com.sun.enterprise.security.auth.realm.file.FileRealm --property file=${com.sun.aas.instanceRoot}/config/mdm-keyfile:jaas-context=MDMRealm:assign-groups=Guest MDMRealm
```

The following command will set the `MDMRealm` as default. This is necessary if you want to use basic authentication.

asadmin set server-config.security-service.default-realm=MDMRealm

To be able to login you need to explicitly add users to the `MDMRealm`. 

Here we add the user `MdmUser`
```
asadmin create-file-user --authrealmname MDMRealm --groups Admin:DescriptiveDataAuthor:Guest MdmUser
```

Currently three roles are supported by openMDM, adapt the users according to your needs
* **Admin**: Administrator role allowed to access the Administration section in the web application
* **DescriptiveDataAuthor**: Users allowed to edit context data
* **Guest**: Default application users allowed to browse and read measurement data

Keep in mind that the roles are mainly used to enable/disable certain actions in the web application.
Authorization in openMDM is still done via the delegation approach, e.g. the adapters are responsible for handling authorization on data.

Next you need to add the following snippet to your `login.conf` in `${com.sun.aas.instanceRoot}/config/`

```
MDMRealm {
  com.sun.enterprise.security.auth.login.FileLoginModule required;
};
```

As a last step you have to provide the credentials of the technical user in adapter configuration in `service.xml`.
For the ODS adapter you have to specify the parameters `user` and `password` of the technical user. For example
```
<service entityManagerFactoryClass="org.eclipse.mdm.api.odsadapter.ODSHttpContextFactory">
    ...
	<param name="user">sa</param>
	<param name="password">sa</param>
	...
</service>
```

Make sure to restart Glassfish afterwards.

### Usage of API tokens

API tokens can be used to authenticate against the HTTP-API when accessing it with a script.
The user can create a API token in the webclient which then can be used for authentication in a script.
Thus the user does not need to specify its password in the script, but the scripts runs with the privileges of the user.
If the token is compromised, it can be deleted in the web client and recreated.

To setup authentication with API tokens the following adaptions have to be made:

Create an additional authentication realm in the Glassfish asadmin console:

```
create-auth-realm --classname com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm --property datasource-jndi=jdbc/openMDM:digest=SHA256:user-table=API_TOKEN:user-name-column=TOKEN_USER:password-column=HASHED_TOKEN:group-table=API_TOKEN_GROUP:group-name-column=GROUP_NAME:group-table-user-name-column=TOKEN_USER:jaas-context=MDMRealm ApiTokenRealm
```

Next you need to adapt the MDMRealm in your `login.conf` in `${com.sun.aas.instanceRoot}/config/`

```
MDMRealm {
  org.eclipse.mdm.application.TokenJDBCLoginModule sufficient realmName="ApiTokenRealm";
  com.sun.enterprise.security.auth.login.FileLoginModule required;
};
```

Name of the login context, the File realm and the realm specified in the web.xml should have the same value. In our case `MDMRealm`.

Make sure to have the database tables `API_TOKEN` and `API_TOKEN_GROUP` in your `openmdm` database schema.
If not you can find the SQL statements in sql files in the zip file of openMDM.

API tokens can then be created in the openMDM web client in the user preferences tab.

After creation you receive a token, which can be used as password along with the token creators' username to authenticate against the openMDM HTTP API.

The session will have the same rights and privileges as the user who created the token.
If a token is not used anymore, it should be deleted with the web client.

### Remove MDMLoginRealm from previous versions

In versions 5.0.0M1, 0.10 and older the configuration of a custom login realm was necessary. If you configured your Glassfish server instance
for one of these versions, you can remove the old configuration options and artifact, as they are no longer needed.

#### Steps to delete the old configuration and artifacts

* Delete the JAR file `org.eclipse.mdm.realm.login.glassfish-VERSION.jar` from `GLASSFISH_ROOT/glassfish/domains/domain1/lib`
* Open the Glassfish login **configuration file** at `GLASSFISH_ROOT/glassfish/domains/domain1/config/login.conf`
* Delete custom MDM realm module entry to this config file
  ```
  MDMLoginRealm {
    org.eclipse.mdm.realm.login.glassfish.LoginRealmModule required;
  };
  ```
* Remove the `MDMLoginRealm` by executing `asadmin delete-auth-realm MDMLoginRealm` or by deleting it in the Glassfish web console

## Configure logging

MDM 5 uses SLF4J and Logback for logging. The default configuration file can be found at `org.eclipse.mdm.nucleus/src/main/resources/logback.xml`.
It logs INFO level messages to `mdm5.log` in the **logs** folder of the Glassfish domain.
If you want to customize logging, you can either edit the file within the WAR file or preferably provide your own logging configuration via system parameter in the JVM settings in Glassfish `-Dlogback.configurationFile=/path/to/config.xml`

## Available REST URLs

Please use the generated OpenAPI Specification, which is generated at build time.
The OpenAPI Specification is available in `org.eclipse.mdm.nucleus/build/openapi/openapi.json` or at runtime at `http://{SERVER}:{PORT}/{APPLICATIONROOT}/openapi.json`.
Furthermore a Swagger UI is available at `http://{SERVER}:{PORT}/{APPLICATIONROOT}/swagger.html`

### Structure of the URLs

* `SERVER` the hostname of the Glassfish server
* `PORT` the port of the Glassfish server
* `APPLICATIONROOT` is the context root under which MDM is deployed
* `SOURCENAME` is the source name the underlying data source

#### Environment

* `http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/environments`
* `http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/environments/{SOURCENAME}`
* `http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/environments/{SOURCENAME}/localizations`

#### Business objects

The following parameters are recurring throughout the different URLs

* `ID` the identifier of the business object

* `CONTEXTTYPE` is one of
  * `unitundertest`
  * `testsequence`
  * `testequipment`

* `DATATYPE` is one of
  * `STRING`
  * `STRING_SEQUENCE`
  * `DATE, DATE_SEQUENCE`
  * `BOOLEAN`
  * `BOOLEAN_SEQUENCE`
  * `BYTE`
  * `BYTE_SEQUENCE`
  * `SHORT`
  * `SHORT_SEQUENCE`
  * `INTEGER`
  * `INTEGER_SEQUENCE`
  * `LONG`
  * `LONG_SEQUENCE`
  * `FLOAT`
  * `FLOAT_SEQUENCE`
  * `DOUBLE`
  * `DOUBLE_SEQUENCE`
  * `BYTE_STREAM`
  * `BYTE_STREAM_SEQUENCE`
  * `FLOAT_COMPLEX`
  * `FLOAT_COMPLEX_SEQUENCE`
  * `DOUBLE_COMPLEX`
  * `DOUBLE_COMPLEX_SEQUENCE`
  * `FILE_LINK`
  * `FILE_LINK_SEQUENCE`

* `FILTERSTRING` is a String defining a filter. For example `Test.Name eq "t*"` filters for all tests which names begin with `t`.
 Strings should be quoted with `"`. `"` characters within strings have to be escaped with a backslash. For backward compatibility `'` is also allowed to quote strings, but may be blocked in URLs in some environments, thus `"` should be preferred.

* `REMOTE_PATH` is the remote path of a file link as it is returned in the attributes of type `FILE_LINK` and `FILE_LINK_SEQUENCE`
 Make sure to properly URL escape the value of the remote path. Especially slashes have to be escaped with `%2F`.

##### Examples

Most of the Business objects support the following calls (examples for **TestStep**)

* `http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/environments/{SOURCENAME}/teststeps?filter={FILTERSTRING}`
* `http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/environments/{SOURCENAME}/teststeps/searchattributes`
* `http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/environments/{SOURCENAME}/teststeps/localizations`
* `http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/environments/{SOURCENAME}/teststeps/{ID}`
* `http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/environments/{SOURCENAME}/teststeps/{ID}/contexts/{CONTEXTTYPE}`

For **TestStep** and **Measurement** it is also possible to receive files

* `http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/environments/{SOURCENAME}/teststeps/{ID}/files/{REMOTE_PATH}`


#### Query endpoint

* `http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/query`
 Example: `curl -POST -H "Content-Type: application/json" -d '{"resultType": "test", "columns": ["Test.Name", "TestStep.Name"], "filters": { "sourceName": "SOURCENAME", "filter": "Test.Id gt 1", "searchString": ""}}'http://sa:sa@localhost:8080/org.eclipse.mdm.nucleus/mdm/query`

* `http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/suggestions`
 Example: `curl -POST -H "Content-Type: application/json" -d '{"sourceNames": ["SOURCENAME"], "type": "Test", "attrName": "Name"}' http://sa:sa@localhost:8080/org.eclipse.mdm.nucleus/mdm/suggestions`


#### ATFX Import

POST: `http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/environments/{SOURCENAME}/import?{property_key1}={property_value1}&{property_key2}={property_value2}`

Imports an ATFX file into the specified environment. The ATFX file must conform to the openMDM 5 data model. Context attributes will only be imported, if Test and TestStep templates are specified. See section [Template lookup](#template-lookup). Properties for the importer can be provided as query parameters.

Three content types are accepted
* `application/xml`: The ATFX file is sent in the body of the POST request.

* `multipart/form-data`: The ATFX file and its binary files are provided as multipart form data. The field name represents the original file name and the field value the contents of the file.
 The ATFX file is detected by its file extension 'atfx' or the media type 'application/xml'. Only one ATFX file can be sent at a time.

* `application/zip`: The ATFX file and its component files are provided as a zip file. The ATFX file is detected by its file extension 'atfx'. Only one ATFX file can be included in the zip file.

The POST request returns a JSON-Object with the following properties
* state: Status of the import. Either `OK` or `FAILED`.
* message: Error message in case the import fails.
* stacktrace: Stacktrace in case the import fails.

The returned HTTP status is either `200`, if the imported succeeded or `400`, if the import failed.

##### Template lookup
Templates will be automatically picked up, from a template attribute. The name of the TestTemplate used for creating the Test will be read from Test.template. The name of the TestStepTemplates used will be read from TestStep.template. Make sure to also extend the Application Model of the ATFx file accordingly:

```
<?xml version='1.0' encoding='UTF-8'?>
<atfx_file ...>
  ...
  <application_model>
    <application_element>
      <name>Test</name>
      ...
      <application_attribute>
        <name>template</name>
        <datatype>DT_STRING</datatype>
      </application_attribute>
    ...
    <application_element>
      <name>TestStep</name>
      ...
      <application_attribute>
        <name>template</name>
        <datatype>DT_STRING</datatype>
      </application_attribute>
    ...
  <instance_data>
    <Test>
      <Id>1</Id>
      <template>testTemplateName</template>
      ...
    </Test>
    <TestStep>
      <Id>1</Id>
      <template>testStepTemplateName</template>
		...
    </TestStep>
    ...
```

#### ATFX Export

POST: `http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/export`

Accepts a shopping basket XML file and exports the contained elements into an ATFX file.
Currently it is only supported to export objects from one environment.

Returned content types in response:
* `application/xml`: The response body contains the exported ATFX file

* `application/zip`: The response contains a zip file with the exported ATFX file and accompanying component files.

### Example: File download

* Login
 `curl -XPOST --cookie-jar cookie -H "Content-Type: application/x-www-form-urlencoded" -d j_username=sa -d j_password=sa http://localhost:8080/org.eclipse.mdm.nucleus/j_security_check`

* Request a test and extract the remotePath from the first file reference
 `FILE_PATH="$(curl --cookie cookie -s http://localhost:8080/org.eclipse.mdm.nucleus/mdm/environments/MDMNVH/teststeps/10 | jq -r '.data[0].attributes[] | select (.name | contains("MDMLinks")) | .value[0].remotePath')"`

* Urlescape remotePath if necessary
 `FILE_PATH="$(echo $FILE_PATH | python -c 'import sys,urllib;print urllib.quote(sys.stdin.read().strip(), "")')"`

* Request the file content
 `curl --cookie cookie http://localhost:8080/org.eclipse.mdm.nucleus/mdm/environments/MDMNVH/teststeps/10/files/$FILE_PATH`

#### CSV Export

* Login
 `curl -XPOST --cookie-jar cookie -H "Content-Type: application/x-www-form-urlencoded" -d j_username=sa -d j_password=sa http://localhost:8080/org.eclipse.mdm.nucleus/j_security_check`

* Export CSV with simocolon as delimiter
`curl --cookie cookie -s -X POST http://localhost:8080/org.eclipse.mdm.nucleus/mdm/export/csv?delimiter=semicolon -d=@shoppingbasket.xml`

### ODS Event Stream

The base URL to access the ODS event streaming api is

`http://{SERVER}:{PORT}/{APPLICATIONROOT}/mdm/events/{SOURCENAME}/{REFERENCEID}`

The reference id must be unique and provided by the client. It is the identifier on the server side to collect the correct events on the requested types.

##### 1. Login

A login must be executed first in order to register and retrieve ODS events.

##### 2. Register for events

This will register to listen on events on the provided reference id.

POST: A http post request must be sent to the base URL. It produces and consumes `application/json` which must be set in the header.

Headers: `Content-Type: application/json`

Body:
Type JSON:
`{"types":["INSTANCE_CREATED","INSTANCE_MODIFIED","INSTANCE_DELETED","MODEL_MODIFIED","SECURITY_MODIFIED"],"entityTypes":["Test", "TestStep"]}`

To register only for specific event types the `types` array can be filled with only the types of interest.

To register for all events the body element can also be left empty.

To register for specific entityTypes the `entityTypes` array can be filled with the names of the root entity types of interest.

If left empty, events for all root entityTypes will be delivered.

##### 3. Delete registration

This will de-register on the provided reference id.

DELETE: A http delete request must be sent to the base URL.

##### 4. Retrieve events

This will retrieve the collected events. Upon invocation of this URL the cached events will be cleared and delivered to the requesting client.

GET: A http get request must be sent to the base URL. It produces `application/json` which must be set in the header.


``Example Response``

```
event: ODS_notification
id: 71489cd2-dd0b-4aa4-9e68-ad0ead91ce1d
data: {
  "received": 1650964578173,
  "type": "instanceModified",
  "mdmEntity": {
    "type": "TestStep",
    "data": [
      {
        "name": "DurationTest",
        "id": "122",
        "type": "TestStep",
        "sourceType": "TestStep",
        "sourceName": "NVHDEMO",
        "attributes": [
          {
            "name": "Sortindex",
            "value": "1",
            "unit": "",
            "dataType": "INTEGER"
          },
          {
            "name": "Description",
            "value": "",
            "unit": "",
            "dataType": "STRING"
          },
          {
            "name": "MDMLinks",
            "value": "",
            "unit": "",
            "dataType": "FILE_LINK_SEQUENCE"
          },
          {
            "name": "Optional",
            "value": "true",
            "unit": "",
            "dataType": "BOOLEAN"
          },
          {
            "name": "DateCreated",
            "value": "2021-08-24T09:43:05Z",
            "unit": "",
            "dataType": "DATE"
          },
          {
            "name": "MimeType",
            "value": "application/x-asam.aosubtest.teststep",
            "unit": "",
            "dataType": "STRING"
          },
          {
            "name": "Name",
            "value": "DurationTest",
            "unit": "",
            "dataType": "STRING"
          }
        ],
        "relations": [
          {
            "name": null,
            "type": "MUTABLE",
            "entityType": "TemplateTestStep",
            "contextType": null,
            "ids": [ "65" ]
          },
          {
            "name": null,
            "type": "MUTABLE",
            "entityType": "Classification",
            "contextType": null,
            "ids": [ "14" ]
          }
        ]
      }
    ]
  },
  "user": {
    "phone": "",
    "mail": "",
    "givenName": "MDM basic example",
    "surname": "sa",
    "department": "",
    "sourceName": "NVHDEMO",
    "mimeType"
  }
}
```

## Preference Service
Preference service stores its data to a relational database. The database connection is looked up by JNDI and the JNDI name and other database relevant parameters are specified in `src/main/resources/META-INF/persistence.xml`. The default JNDI name for the JDBC resource is set to `jdbc/openMDM`. This JDBC resource and its dependent JDBC Connection Pool has to be created and configured within the Glassfish web administration console or through `asadmin` command line tool.

Furthermore the schema has to be created in the configured database. Therefore database DDL scripts are available for PostgreSQL and Apache Derby databases in the folder `schema/org.eclipse.mdm.preferences` of the distribution.
Other databases supported by EclipseLink may also work, but is up to the user to adapt the DDL scripts.

### Examples
* Receive values
 `curl -GET -H "Content-Type: application/json" http://localhost:8080/org.eclipse.mdm.nucleus/mdm/preferences?scope=SYSTEM&key=ignoredAttributes`

* Set value
 `curl -PUT -H "Content-Type: application/json" -d '{"scope": "SYSTEM", "key": "ignoredAttributes", "value": "[\"*.MimeType\"]"}' http://localhost:8080/org.eclipse.mdm.nucleus/mdm/preferences`

* Delete entry
 `curl -DELETE http://localhost:8080/org.eclipse.mdm.nucleus/mdm/preferences/ID`


## FreeTextSearch
### Configuration
1. Start ElasticSearch. ElasticSearch can be downloaded at https://www.elastic.co/products/elasticsearch. The minimum supported Elasticsearch version is 7. For testing purpose, it can be simply started by executing `bin/run.sh`

2. Edit the configuration (`global.properties`) to fit your environment. You need an ODS Server which supports Notifications. All fields have to be there, but can be empty. However certain ODS Servers ignore some parameters (e.g. PeakODS ignores pollingIntervall since it pushes notifications).

3. Start the application. At the first run it will index the database. This might take a while. After that MDM registers itself as `NotificationListener` and adapts all changes one-by-one.

### Run on dedicated server

The Indexing is completely independent from the searching. So the Indexer can be freely deployed at any other machine.
In the simplest case, the same steps as in Configuration have to be done.
The application can then be deployed on any other machine. All components besides the FreeTextIndexer and its dependencies are not user. Those can be left out, if desired.

### Control background indexing ###

In the MDM application the background indexing can be controlled via preferences on system level.

Therefore the following parameter patterns are used:

 `freetextindexer.background.<entityclass>.<free_text>=<day_upper>,<day_lower>,<only_every_n_days>`
 `freetextindexer.background.batchsize=<amount_of_entities_in_one_batch>`

Entity class can be one of the following values: test, teststep, measurement


#### Configuration Examples ####

Definition for the entity test to be indexed if it is created within the last 30 days. The index should be updated once each day.

 `freetextindexer.background.test.1month=0,-30,1`

Definition for the entity test to be indexed if it is created between the last 30 days and the last 180 days. The index should be updated every 10 days.

 `freetextindexer.background.test.halfyear=-30,-180,10`

In each batch run, the indexer should process 50 tests, test steps and measurements

 `freetextindexer.background.batchsize=50`


## Connector and service.xml

The `service.xml` contains all information necessary for the Connector-Service to connect to the available datasources/adapter instances.
The `service.xml` changed in version 5.2.0M5 and since then the `service` tag needs the mandatory attribute `sourceName` which is used to distinguish the datasource and is used to select datasource in the webclient.
This version 5.3.0 it is possible to use the HTTP API to connect to the ODS server instead of CORBA.

To use HTTP API you have to configure `org.eclipse.mdm.api.odsadapter.ODSHttpContextFactory` as `entityManagerFactoryClass` and provide the `url` as parameter:

````
	<service sourceName="YOUR_SERVICE1" entityManagerFactoryClass="org.eclipse.mdm.api.odsadapter.ODSHttpContextFactory">
		<param name="url">https://YOUR_HOST1:443/api/</param>
		<param name="user">sa</param>
		<param name="password">sa</param>
	</service>
````

To use CORBA API you have to configure `org.eclipse.mdm.api.odsadapter.ODSContextFactory` as `entityManagerFactoryClass` and provide the `nameservice` and `servicename` as parameters:

````
	<service sourceName="YOUR_SERVICE5" entityManagerFactoryClass="org.eclipse.mdm.api.odsadapter.ODSContextFactory">
		<param name="nameservice">corbaloc::1.2@YOUR_HOST5:2809/NameService</param>
		<param name="servicename">YOUR_SERVICE5.ASAM-ODS</param>

		<param name="user">sa</param>
		<param name="password">sa</param>
	</service>
````

Since the information in `service.xml` includes secret information like passwords, it is possible to provide lookups, which gives you the possibility to specify tokens as references to properties defined elsewhere.

There are different lookups available
* `sys`: Looks up variables defined as system properties
* `env`: Looks up variables defined as environment variables

Example:

`<param name="password">${env:odsPassword}</param>`


## Known issues

If you run into `java.lang.ClassNotFoundException: javax.xml.parsers.ParserConfigurationException not found by org.eclipse.persistence.moxy` this is a bug described in https://bugs.eclipse.org/bugs/show_bug.cgi?id=463169 and https://java.net/jira/browse/GLASSFISH-21440.
This solution is to replace `GLASSFISH_HOME/glassfish/modules/org.eclipse.persistence.moxy.jar` with this file http://central.maven.org/maven2/org/eclipse/persistence/org.eclipse.persistence.moxy/2.6.1/org.eclipse.persistence.moxy-2.6.1.jar

If you run into `java.lang.ClassNotFoundException: com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector not found by com.fasterxml.jackson.jaxrs.jackson-jaxrs-json-provider` you have to download http://central.maven.org/maven2/com/fasterxml/jackson/module/jackson-module-jaxb-annotations/2.5.1/jackson-module-jaxb-annotations-2.5.1.jar and put in the `GLASSFISH_HOME/glassfish/domains/domain1/autodeploy/bundles` folder

## Client preferences

The applications preferences are managed in the administration section. This section can be accessed via the **Administration** button in the main navigation bar or via http://localhost:8080/org.eclipse.mdm.nucleus/administration

A preference is a pair of a unique key and a value. The key is composed of a prefix defining the purpose of the preference followed by an arbitrary but unique identifier string.
It is recommended to choose the identifier the same as the preferences `name` field, in case there is one. The value holds the preference's data in a JSON string.

The following preferences, sorted by their scope, can be set

* User
  - Basket
  - View
  - Filter

* System
  - Node provider
  - Shopping basket file extensions

* Source
  - Ignored attributes

However, it might be necessary to reload the application before a newly defined preference is available or any changes on an existing preferences are applied.

**WARNING**: Corrupted preferences can result in malfunctions of the application.

### User scope

A user scoped preference's area of effect is limited to the logged in user. All user scoped preferences can also be set in dialogs in the main application.

1. Basket
 Basket preferences keys must start with the prefix `basket.nodes.`
 This preference has the fields `items` and `name` and holds all the information for saved baskets.
   * The field `items` holds an array of MDMItems, providing the relevant information of a related node, i.e. `source`, `type` and `id`.
   * The field `name` defines the name, which is provided in the main application to load this basket.

   Example
   ```
   {
     "items": [{"source":"MDMNVH","type":"Test","id":38}],
     "name": "basketExample"
   }
   ```

2. View
  View preferences keys must start with the prefix `tableview.view.`
  This preference has the fields `columns` and `name` and holds the layout information for the tables displaying the search results and the basket nodes.
    * The field `columns` holds an array of ViewColumn objects. A ViewColumn is an Object with the fields `type`, `name`, `sortOrder` and an optional field `style`.
    * The field `type` can be set to all available MDM data types, i.e. `Project`, `Pool`, `Test`, `TestStep`, `Measurement`, `ChannelGroup`, `Channel`.
    * The field `name` field specifies an attribute, which must be an searchable attribute for the given `type`.
    * The field `sortOrder` can be set by the number `1` (ascending), `-1` (descending), or null (unsorted). Only one column of the array can have a non-null value `sortOrder` at a time. The ViewColumn's style element can hold any CSS-style object. However, it is supposed to contain only the columns width. The column order in the array is identically with the appearance in the table.
    * The field `name` defines the name, which is provided in the main application to load this view.

    Example
    ```
    {
     "columns": [
       {
         "type": "Test",
         "name": "Id",
         "style": {"width":"75px"},
         "sortOrder": null
       }
     ],
     "name": "viewExample"
    }
    ```

3. Filter
  Filter preferences keys must start with the prefix `filter.nodes.`
  This preference has the fields `conditions`, `name`, `environments`, `resultType` and `fulltextQuery`.
  It provides the information for the attribute based / advanced search.
    * The field `conditions` holds an array of Condition objects. A Condition specifies a search condition for attribute based search. It consists of the fields `type`, `name`, `operator`, `value` and `valueType`. The Condition's `type` can be set to all available MDM data types, i.e. `Project`, `Pool`, `Test`, `TestStep`, `Measurement`, `ChannelGroup`, `Channel`.
      * The Condition's `name` field specifies an attribute, which must be an searchable attribute for the given `type`.
      * The Condition's `operator` field, holds on of the following numbers: `0`(=), `1`(<), `2`(>), `3`(like).
      * The Condition's `value` field holds a string array containing input for the attribute based search.
      * The Condition's `resultType` field should match the type corresponding to the attribute specified in the `'name` filed, e.g. `string`, `date` or `long`.
    * The field `name` defines the name, which is provided in the main application to load this filter.
    * The field `environments` holds an string array with the names of the sources that should be included in the search.
    * The field `resultType` can be set to all available MDM data types (see above). Only nodes of this type will be included in the search.
    * The field `fulltextQuery` holds a string containing full text search input.

    Example
    ```
    { "conditions": [
        {
          "type": "Test",
          "attribute": "Name",
          "operator": 0,
          "value": [],
          "valueType":"string"
        }
      ],
      "name": "filterExample",
      "environments": ["sourceName"],
      "resultType": "Test",
      "fulltextQuery": ""
    }
    ```


### System scope

System scoped preference are applied globally.
#### Restore tree state
  The  navigator is able to save the state of the tree in the Browsers local storage and reopen the tree in the saved state after page reload or logout.

  The parameter `navigator.restoreTreeState` with value `true` will save the state of the tree.

#### Default result type in search component

The default result type selected in the search component can be defined by a system scoped preference with key `search.default_values` and value
`{"resultType": "<ResultType>"}` where `<ResultType>` can be one of:
* Test
* TestStep
* Measurement
* ChannelGroup
* Channel

#### Node provider
  The navigation tree structure can be defined via a node provider. The default node provider is set in `nucleus/webclient/src/main/webapp/src/app/navigator/defaultnodeprovider.json`.
  It is recommended not to change the default node provider. Instead new node providers can be added as preferences.
  Their keys must start with the prefix `nodeprovider.`. Once a custom node provider is supplied it can be selected in the dropdown menu in the navigation tree header.

##### Structure

* First layer/root nodes
In the node provider each layer of nodes of the navigation tree is defined in a nested object.
The first layer of nodes, is always the environment level.
The first layer consists of the fields `id`, `name`, `type`, and `children`.
* The field `id` sets the id, which is used to uniquely identify the nodeprovider.
* The field `name` sets the name, which is displayed in the application to select the corresponding node provider.
* The field `type` defines the data type of the nodes, which is always `Environments` on the first layer.

The next layer of nodes are defined via the field `children`.

* Children
A child object consists of the fields `type`, and `children`. Optional fields include `filterAttributes`, `labelAttributes`, `labelExpression`, `virtual` and `contextState`.
* The field `type` sets the data type of this layer of nodes. It can be set to all available MDM data types, i.e. `Project`, `Pool`, `Test`, `TestStep`, `Measurement`, `ChannelGroup`, `Channel`.
* The `children` field does not need to be set for the last layer of nodes.
* The array field `filterAttributes` defines what attributes to use as a filter condition for this node layer. A filter attribute can either be the name of the attribute or an object with properties `name`, `operator`, `value`, like `{"name": "Id", operator: "EQUAL", "${Id}"}`
* `labelAttributes` is an array of attributes to provide for the `labelexpression`
* The field `labelExpression` can be used to override the default label with a user defined expression (in Java Expression Language). The attributes given in `labelAttributes` can be accessed as variables.
* The boolean field `virtual` indicates if the node is virtual.
* The field `contextState` can have the values `MEASURED` (default) and `ORDERED`.

##### Examples
* Minimal node provider

 ```
{ "id" : "min_nodepvoider", "name": "My name to display", "type": "Environment"} ```

 * Node provider with which does not display Pools and ChannelGroups and uses a label Expression to display TestStep name and id in the TestStep label.

```
  "id": "example_nodepvoider",
  "name": "My name to display",
  "type": "Environment",
  "children": {
    "type": "Project",
    "children": {
      "type": "Test",
      "children": {
        "type": "TestStep",
        "labelAttributes": ["Name", "Id"],
        "labelExpression": "${Name} (${Id})"
        "children": {
          "type": "Measurement",
          "children": {
            "type": "Channels"
          }
        }
      }
    }
  }
```

*#### Shopping basket file extensions

  When downloading the contents of a shopping basket, a file with extension `mdm` is generated.
  Additional file extensions can be adding by poviding a preference with key `shoppingbasket.fileextensions`.
  Here you can define a list of objects with attributes `label` and `extension`.
  For example: `[ { "label": "MyTool", "extension": "mdm-mytool" }, { "label": "OtherTool", "extension": "mdm-other" } ]`.
  If `MyTool` has a file handler registered for the extension `mdm-mytool`, the application will be launched if the browser automatically opens the file after download.

*#### Quick viewer

  The quick viewer can be configured to limit the initial number of displayed channels. This is especially useful if
  it is known that many measurement channels are available in the system.

  The parameter `chart-viewer.channels.max-display` will limit the initial number.

  The parameter `chart-viewer.channels.load-interval` will limit the amount of channels which can be loaded subsequently.

*#### Parameter Viewer

  The Parameter Viewer in the details tab supports reading legacy ResultParameterSets and new Measurement- and ChannelParameterSets.
  It will automatically use new Measurement- and ChannelParameterSets, if they are present in the ODS model.

  If the parameter `parameterset-viewer.use_legacy` is present and set to `true`, ResultParameterSets are used.


### Source scope

Source scoped preferences are applied at any user, but are limited to the specified source. The source can be specified in the **Add Preference** or **Edit Preference** dialog.

* Ignored Attributes
  The ignore attributes preference must have the exact key `ignoredAttributes`. An identifier must not be added.
  The preference specifies all attributes, which are supposed to be ignored in the detail view. The preference is a simple JSON string holding a list of attributes in the form {"<type>.<AttributeName>"}.
  The placeholders <type> and <AttributeName> have to be replaced by the actual type and name of the attribute which should be ignored, respectively.

  Example:
  `["*.MimeType", "TestStep.Sortindex"]`

## Create a module for the Web application

Any MDM module needs to be a valid [Angular module](https://angular.io/guide/architecture-modules) aka NgModule.
A NgModule consists of the module definition, components, services and other files that are in the scope of the module.
The component can hold any content. The component must be declared in a module definition to grant accessibility in the rest of the application.
All related files should be stored in a new module subfolder in the app folder `nucleus/webclient/src/main/webapp/src/app` (eg. `nucleus/webclient/src/main/webapp/src/app/new-module`)

### Example module

An example for a new module can be found at `nucleus/webclient/src/main/webapp/src/app/example-module`

### Creating a MDM module

1. Create a new folder eg. `nucleus/webclient/src/main/webapp/src/app/new-module`

2. Create an Angular component (eg. `mdm-new.component.ts`) inside that new folder
   ```typescript
    import {Component} from '@angular/core';
    @Component({template: '<h1>Example Module</h1>'})
    export class MDMNewComponent {}
   ```
   A component is defined in a Typescript file with the `@Component()` decorator.
   Any HTML content can be provided here in an inline template or via a link to an external HTML resource. Thereafter the component itself, which is supposed to hold any logic needed, is defined and exported.
   For more details see https://angular.io/guide/architecture-components.

3. Create a minimal NgModule  (eg. `mdm-new.module.ts` ) inside that new folder
   ```typescript
    import { NgModule } from '@angular/core';
    import { MDMCoreModule } from '../core/mdm-core.module';
    import { MDMNewComponent } from './mdm-new.component';
    @NgModule({imports: [MDMCoreModule], declarations: [MDMNewComponent]})
    export class MDMNewModule {}
   ```
   The `imports` array holds all modules from the application needed in this MDM module. It should always hold the `MDMCoreModule`, which provides basic functionalities.
   On the other hand a NgModule grants accessibility of components of this module in other directives (including the HTML template) within the module (in a `declaration` array) or even in other parts of the application (in an `export` array).
   For more details see https://angular.io/guide/architecture-modules.

### Embedding a module (no lazy loading)

To embed this new module in MDM you have to register this module in the `MDMModules` Module.
* Import the new module at `nucleus/webclient/src/main/webapp/src/app/modules/mdm-modules.module.ts`

* Register a route to the new module at `nucleus/webclient/src/main/webapp/src/app/modules/mdm-modules-routing.module.ts`
  ```
  { path: 'new', component: MDMNewComponent}
  ```

 * Furthermore you have to define a display name for the registered route in the `links` array in `nucleus/webclient/src/main/webapp/src/app/modules/mdm-modules.component.ts`
  ```
  { path: 'new', name: 'New Module' }
  ```

For further information refer to the Angular documentation:
* https://angular.io/guide/architecture-components
* https://angular.io/guide/architecture-modules
* https://angular.io/guide/router


### Lazy loading and routing module

For lazy-loading (recommended in case there is a high number of modules) embedding of the module is slightly different.
```
  { path: 'example', loadChildren: '../example-module/mdm-example.module#MDMExampleModule'}
```

Additionally, a NgModule, the so called routing module (eg. `mdm-new-routing.module.ts`), is needed to provide the routes to this modules components.
```typescript
  const moduleRoutes: Routes = [{ path: '', component: MDMExampleComponent }];
  @NgModule({imports: [RouterModule.forChild(moduleRoutes)], exports: [RouterModule]})
  export class MDMExampleRoutingModule {}
```

### Filerelease module
The filerelease module is stored in the following folder `nucleus/webclient/src/main/webapp/src/app/filerelease`

It can be embedded as any other module described above.
```
{ path: 'filerelease', component: MDMFilereleaseComponent }
```

```
{ name: 'MDM Files', path: 'filerelease'}
```

### Adding a module to the detail view (eg. filerelease module)
To make the filerelease module available in the detail view it needs to be imported in the corresponding MDM Module `nucleus/webclient/src/main/webapp/src/app/details/mdm-detail.module.ts`
Thereafter, the `MDMFilereleaseCreateComponent` can be imported to the `nucleus/webclient/src/main/webapp/src/app/details/components/mdm-detail-view/mdm-detail-view.component.ts`.
Then the following has to be added to the `nucleus/webclient/src/main/webapp/src/app/details/components/mdm-detail-view/mdm-detail-view.component.html` file:
```
  <mdm-filerelease-create [node]=selectedNode [disabled]="isReleasable()"></mdm-filerelease-create>
```

It should be located right after the add to basket button:
```
<div class="btn-group pull-right" role="group">
  <button type="button" class="btn btn-default" (click)="add2Basket()" [disabled]="isShopable()">In den Warenkorb</button>
  <mdm-filerelease-create [node]=selectedNode [disabled]="isReleasable()"></mdm-filerelease-create>
</div>
```

## Copyright and License
Copyright (c) 2015-2018 Contributors to the Eclipse Foundation

 See the NOTICE file(s) distributed with this work for additional
 information regarding copyright ownership.

 This program and the accompanying materials are made available under the
 terms of the Eclipse Public License v. 2.0 which is available at
 http://www.eclipse.org/legal/epl-2.0.

 SPDX-License-Identifier: EPL-2.0