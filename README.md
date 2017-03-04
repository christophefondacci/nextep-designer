# Nextep Designer IDE

NeXtep Open Designer is a powerful multi-vendor / multi-platform open source Integrated Development Environment (IDE) for database developments, focused on automation and productivity. It offers to automate database packaging operations by leveraging a version control engine which tracks any change you make to your database applications. It enables simple and safe deployment of your packaged developments through automatic validation of your database structure before and after deployment.

## Get Started

All information to get started with neXtep designer is available on the Wiki pages:
https://github.com/christophefondacci/nextep-designer/wiki

## Supported OS

Windows, linux, macOS.

## Supported database vendors

Oracle, mySQL, PostgreSQL, DB2, SQL Server.

## Download latest binaries

* Linux 32 bits: 
    * https://github.com/christophefondacci/nextep-designer/releases/download/1.0.7.201610130739/neXtep.1.0.7-linux.gtk.x86.zip
* Linus 64 bits:
    * https://github.com/christophefondacci/nextep-designer/releases/download/1.0.7.201610130739/neXtep.1.0.7-linux.gtk.x86_64.zip
* Mac OS X 32 bits:
    * https://github.com/christophefondacci/nextep-designer/releases/download/1.0.7.201610130739/neXtep.1.0.7-macosx.cocoa.x86.zip
* Mac OS X 64 bits:
    * https://github.com/christophefondacci/nextep-designer/releases/download/1.0.7.201610130739/neXtep.1.0.7-macosx.cocoa.x86_64.zip
* MS Windows 32 bits:
    * https://github.com/christophefondacci/nextep-designer/releases/download/1.0.7.201610130739/neXtep.1.0.7-win32.win32.x86.zip
* MS Windows 64 bits:
    * https://github.com/christophefondacci/nextep-designer/releases/download/1.0.7.201610130739/neXtep.1.0.7-win32.win32.x86_64.zip

## NeXtep code organization

NeXtep designer is an Eclipse RCP product. As such, it consists in a stack of plugins which contributes to the environment. Every plugin is represented as an eclipse project. A special plugin is ''com.neXtep.Designer'' which is the top-level entry-point which defines the ''product''. 


In short, here is a rapid overview of the architecture :
* **Core** : core-level features and definitions such as types, controllers, references, dependency management, root level exception
* **Vcs** : Version control system - introduces the notion of versionable elements provides the corresponding beans and versioning services to play with version control. It also provides means of element comparisons, mergers. 
* **Dbgm** : database generic model - introduces the notion of database model elements (table, columns, indexes, etc.) in an common layer.
* **Dbgm._vendor_** : database model vendor-specific extensions - provides vendor-specific database features / elements
* **Sqlgen** : SQL-related definitions - introduces SQL-based elements definition, helpers and services such as SQL scripts, stored procedure / functions / package implementations, SQL generators (transforming a model or a model delta into SQL).
* **Sqlclient** : the SQL client
* **Synch** : synchronization and reverse synchronization services
* **Beng** : Build engine - introduces the notion of neXtep deliveries, exportable packages, delivery dependencies


Every layer is split between UI and non-UI part. The non-UI part provides the model, the services, and will never rely on a UI interaction with the user while the UI part will provide editors, UI controllers, dialogs, wizards, command handlers and any user- related interaction. Typically the UI layer embeds feature of the non UI part and expose them to the user.

## Setting up a development environment


### Configuring Eclipse

The current release neXtep is based on is Eclipse 4.6. For development, we advise you use the ''Eclipse for RCP and RAP Developers'' which will contain the appropriate tools and source code for properly debugging an RCP application. Download this distribution from http://www.eclipse.org/downloads/packages/eclipse-rcp-and-rap-developers/neon2

### Setting up the workspace

Checkout our sources:

`
git checkout https://github.com/christophefondacci/nextep-designer
`

Create a new Eclipse workspace and select file / import then choose "General / Existing projects into workspace" and select the directory where you checked out the sources. All projects should be listed, click finish.

### Running Nextep in debug mode

To create a working debug configuration locate the file com.neXtep.Designer/designer.product in the Eclipse project explorer and open it. In the "Testing" section of the page that will open, click on "Launch an Eclipse application in Debug mode". You're all set!

## Translating neXtep

If you are interested in translating the product you will find here all information you need.


### Translating environment labels and messages

In order to translate labels and messages of the environment, you can have a look at the message files : they all end by "...Messages.properties" and "...Messages_fr.properties" and are located in the root package of every plugin. These files are the resource bundles used for translating labels and messages.
 

For example, you will find version control translations in the "com.neXtep.designer.vcs" plugin, source directory "src", and root package ''com.nextep.designer.vcs'' . Here you will find several properties files :
* VCSMessages.properties : the english default labels
* VCSMessages_fr.properties : the french translations
* VCSMessages_xx.properties : any other language translation


If you want to translate any label of this plugin, you only have to drop your message file with the proper language extension. Be careful of using an UTF-8 charset or to escape every special character by their unicode number.


### Translating contributions

Contributions have a specific translation mechanism which takes advantage of the OSGi localization feature. For this reason, contribution translation are located in a distinct location. Typical labels which are registered as contributions :
* Menu labels and popup menus
* Tooltips on any command
* View names, perspective names
* Element type names


Translations are defined on a per-plugin basis, and located in the ''OSGI-INF/l10n'' folder as ''bundle.properties'' files. Using the same principle as mentionned above, you only have to drop your localized message file ''bundle_xx.properties'' (where ''xx'' is the language code you want to provide translations for) in the folder to provide new translations.


### Plugin fragments

Another option to avoid being intrusive in the existing code and to smoothly plug your translation in neXtep is to provide plugin fragments for every plugin you want to translate. At the moment, we think it would be an overkill for what we need as it would generate tons of new plugin fragments containing only one or 2 files. Feel free to discuss on this topic in the neXtep forum.

## Adding support of new database vendor

### Adding the vendor definition

All vendors are defined through an Enum class called DBVendor which you can find in the ''com.neXtep.designer.core'' plugin : 
com.nextep.designer.core.model.DBVendor

This enumeration defines all database vendors that neXtep supports. In the enum definition, you will have to define a few information such as the default formatter, default client executable name and default port.

Here is what a typical vendor definition looks like :
<pre>
ORACLE("Oracle", IFormatter.UPPERCASE, "sqlplus", 1521)
</pre>

### Adding the connectivity

#### Adding the new JDBC driver

In neXtep, we opted for bundling the JDBC along with the product so that the user is never prompted for a JDBC driver, url pattern, etc.

First, download the vendor's latest production release of their JDBC (if several are available, opt for pure Java). Eclipse provides an easy way to integrate jars as plugin. In the navigator, right clic : ''New… > Others > Plugin from existing jar archive'' and select the driver you downloaded.

Please use the following pattern for JDBC plugins :
com.neXtep.designer.jdbc.''vendor''

#### Adding the database connector

On top of the JDBC we have an abstraction that allows full abstraction of the connectivity features. This abstraction is done thanks to the ''com.nextep.designer.core.model.IDatabaseConnector'' interface of the core plugin.

To ensure proper organic extension of the neXtep product, you will need to create a new plugin which will represent an extension of neXtep for your specific vendor.

In the neXtep architecture, the connectivity features are generally placed in the ''sqlgen'' plugins along with capturers and generators. You should now create a new Eclipse plugin (''New > Other… > Eclipse plugin project'', default options and '''not contributing to UI''') named on the following pattern :
com.neXtep.designer.sqlgen.''vendor''

Now create a new package where you will put your new connector : 
com.nextep.designer.sqlgen.''vendor''.impl

Create a new class implementing IDatabaseConnector and extending AbstractDatabaseConnector which we will use as a base foundation. Implement the various methods depending on your JDBC's specificities.

Here is an example of the DB2 connector class :
<pre>
public final class DB2DatabaseConnector extends AbstractDatabaseConnector {

	private static final Log LOGGER = LogFactory.getLog(DB2DatabaseConnector.class);
	private static final String DB2_JDBC_DRIVER_CLASSNAME = "com.ibm.db2.jcc.DB2Driver"; //$NON-NLS-1$

	public DB2DatabaseConnector() {
		super();
	}

	@Override
	public Connection connect() throws SQLException {
		String connURL = getConnectionURL();
		LOGGER.info("Connecting to DB2 database from URL [" + connURL + "]..."); //$NON-NLS-2$
		try {
			return driver.connect(connURL, getConnectionInfo());
		} catch (SQLException sqle) {
			throw new ErrorException("Unable to connect to the DB2 database", sqle);
		}
	}

	@Override
	public String getConnectionURL() {
		return "jdbc:db2://" + getHost() + ":" + getPort() + "/" + getSid(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public String getJDBCDriverClassName() {
		return DB2_JDBC_DRIVER_CLASSNAME;
	}

	@Override
	public Collection<ErrorInfo> showErrors(String objName) {
		// FIXME [BGA]: Check how to fetch errors in DB2
		return Collections.emptyList();
	}

}
</pre>

#### Registering the database connector

Now that you've built your connector, it is time to register it. We do this through eclipse's extension mechanism. Open the ''plugin.xml'' file at the root of your sqlgen.''vendor'' plugin project. In the editor, go in the ''Extensions'' tab and clic on 'Add…'. Select ''com.neXtep.designer.core.dbConnector''. The definition consists in 2 information :
* The vendor code (= the code of the enum you defined earlier) 
* The connector class you have just created

Save the file and it's done!
 
### First validation

At this step, neXtep should start to work with your vendor with all default features active. Launch the neXtep product in debug mode (don't forget to edit your debug configuration in order to add your new JDBC and sqlgen plugin in the 'Plug-ins' tab of the debug configuration).

We advise you to start to validate the vendor on a repository that you create for another vendor. It will be easier to validate that everything work before trying to deploy a repository on the new vendor.

Start neXtep designer and try to create a new workspace from an existing database in the workspace selection dialog. You should now see your new vendor in the connection's dialog vendor combo. Select all the information and clic ''Test connection''. If this works, it means that your IDatabaseConnector works fine ! If it is not, analyze the logs and fix problems in your connector.

You should be able to :
* Import an existing database for the new vendor you added (only basic objects like tables, columns, indexes, FK, primary and unique keys)
* Synchronize the repository with any database (same scope)
* Generate SQL from version information or from synchronization : note that the syntax will be ANSI 92 and many features like column alteration, rename, etc will not be supported


If your new vendor supports ANSI92 SQL you should then be able to deploy a neXtep repository on it.

### Implementing your vendor-specific features

If your validation was successful, you can see that nextep has a flexible architecture which allows it to automatically default most of the intern mechanisms. Now you will see that everything is extendable in order to allow you to implement the specificities of your vendor step by step by extending default mechanisms. 
We will now see the elements you should customize for your vendor.
 
#### Adding a parser

The first specific implementation you will need to provide for proper integration is a parser definition which will provide the database's reserved keywords, command tags, and some other information used by the editors and the generators. A parser is materialized by the interface : ''com.nextep.designer.sqlgen.model.IParser' :
* The procedure is exactly the same as what you did for the database connector implementation :
** Create a specific parser implementation
** Declare it through the extension point ''com.neXtep.designer.sqlgen.sqlParser''

#### Adding a datatype provider

The datatype provider is the entity defining the types allowed by your vendor. In addition, it also provide information about which types are numeric, which are dates, which are strings, which one could be sized, etc. Again, same procedure to define it through the ''com.nextep.designer.dbgm.datatypeProvider'' extension point.

#### Adding a vendor-specific generator

With the configuration you setup, you are working using the default implementations of almost everything : ANSI generators, JDBC capturers, default mergers. You may want to start taking advantage of your new vendor's SQL dialect to allow more SQL operations. Unfortunately, we don't have time to document the whole procedure here so we will provide some hints of how you can do some customization :
* Start by looking at the ISQLGenerator interface, it allows all kind of SQL generation
* Generators are typed by elements (tables, views, columns, indexes, etc.), so start by looking at the generation you want to work on and look at what it does, how it does it, and what it does not. Generally the default generators will be called ''TableGenerator'', ''IndexGenerator'', ''ColumnGenerator'' while specific generators are called ''OracleTableGenerator'', or ''MySqlIndexGenerator''.
* Note that extensions may or may not exist for all vendors or for all element types. You can choose to only provide a generator for columns so that all default generators will be used except when it needs to generate the SQL of a column.
* Generators are registered through the extension point ''com.neXtep.designer.sqlgen.sqlGenerator''. In the extension you define the type id, the vendor restriction and the implementation of the ISQLGenerator.
* Look at how this has been done to provide specific generators for DB2, Oracle, or Mysql for example. You'll have plenty of examples.

#### Adding a vendor-specific capturer

As you will notice, by default neXtep will not retrieve all objects of your database schema, only the information it could access through JDBC. If you want to be able to capture more information from the database, you will need to define a specific capturer for your vendor. 

The interface you need to provide is a ICapturer which is the way to provide objects from a database connection to the environment. Look at the implementation of this class for other vendors (except Oracle which has not fully been refactored yet). Compose this capturer with the JDBCCapturer and specialize what you need.

Once you are done, you will be able to register your new capturer for your vendor through the extension point ''com.neXtep.designer.sqlgen.sqlCapturer''
