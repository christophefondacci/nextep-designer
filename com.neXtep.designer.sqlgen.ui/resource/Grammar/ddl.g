class MyParser extends Parser; // definición de un analizador sintáctico

start_rule: (statement);
statement :
	("<<" label_name ">>")?
	( assignment_statement
	| close_statement
	| execute_immediate_statement
	| execute_statement
	| explain_plan_statement
	| exit_statement
	| fetch_statement
	| forall_statement
	| goto_statement
	| if_statement
	| loop_statement
	| null_statement
	| open_for_statement
	| open_statement
	| function_declaration
	| procedure_declaration
	| plsql_block
	| rename_statement
	| raise_statement
	| return_statement
	| call_statement
	| noaudit_statement
	| associate_statistics
	| create_type
	| create_type_body
	| create_operator
	| create_table
	| create_tablespace
	| create_temporary_tablespace
	| create_trigger
	| create_cluster
	| create_context
	| create_controlfile
	| create_index
	| create_indextype
	| create_database_link
	| create_database
	| create_dimension
	| create_directory
	| create_library
	| create_outline
	| create_pfile
	| create_synonym
	| create_package
	| create_package_body
	| create_procedure
	| create_profile
	| create_role
	| create_user
	| create_rollback_segment
	| create_schema
	| create_view
	| create_materialized_view_log
	| create_materialized_view
	| grant_statement
	| set_transaction_statement
	| revoke_statement
	| create_snapshot
	| create_function
	| drop_cluster
	| drop_database_link
	| drop_directory
	| drop_dimension
	| drop_function
	| drop_index
	| drop_indextype
	| drop_context
	| drop_library
	| drop_operator
	| drop_package
	| drop_procedure
	| drop_profile
	| drop_role
	| drop_rollback_segment
	| drop_sequence
	| drop_snapshot
	| drop_synonym
	| drop_table
	| drop_tablespace
	| drop_trigger
	| drop_type
	| drop_type_body
	| drop_user
	| drop_view
	| drop_materialized_view
	| drop_materialized_view_log
	| drop_outline
	| disassociate_statistics
	| case_statement
	| commit_statement
	| delete_statement
	| insert_statement
	| lock_table_statement
	| rollback_statement
	| savepoint_statement
	| select_statement
	| truncate_statement
	| update_statement
	| alter_database_statement
	| alter_cluster_statement
	| alter_dimension_statement
	| alter_function_statement
	| alter_procedure_statement
	| alter_profile_statement
	| alter_index_statement
	| alter_indextype_statement
	| alter_operator_statement
	| alter_outline_statement
	| alter_package_statement
	| alter_resource_statement
	| alter_role_statement
	| alter_rollback_statement
	| alter_sequence_statement
	| alter_session_statement
	| alter_snapshot_statement
	| alter_system_statement
	| alter_table_statement
	| alter_tablespace_statement
	| alter_trigger_statement
	| alter_type_statement
	| alter_user_statement
	| alter_view_statement
	| alter_materialized_view_log
	| alter_materialized_view_statement
	| analyze_statement
	| audit_statement
	| comment_statement
	| merge_statement
	| set_role
	| set_constraints

	// java
	| create_java
	| alter_java_statement
	| drop_java_statement

	| expression
	| ";"
	| "/"
	) (";")?
	|
	"<<" label_name ">>" ;

select_statement :
	(subquery | select_into_statement)
	(for_update_clause)?;

subquery :
	(subquery_factoring_clause)?
	"SELECT" (hint)?
	(distinct_all_or_unique)? select_list
	from_clause
	(where_clause)?
	(hierarchical_query_clause)?
	(group_by_clause)?
	(having_clause)?
	(subquery_clause)?
	(order_by_clause)?
	;

select_into_statement :
	"SELECT"
	(distinct_or_all)? select_list
	select_into_clause
	from_clause
	(where_clause)?
	(hierarchical_query_clause)?
	(group_by_clause)?
	(having_clause)?
	(subquery_clause)?
	(order_by_clause)? ;

from_clause :
	"FROM"
	{   table_reference
	  | ("THE")? "(" subquery ")" (alias)?
	, "," };

having_clause :
	"HAVING" condition ;

subquery_clause :
	subquery_operator
	( "(" subquery ")"
	  | subquery );

subquery_operator :
	  "UNION" ("ALL")?
	| "INTERSECT"
	| "MINUS"
	;

select_item :
	(
	  null_tag
	| numeric_literal
	| qualified_name ("." "*" | "." ("CURRVAL" | "NEXTVAL"))?
	| function_name ("(" {parameter_name, ","} ")")?)
	| squotedstring
	(("AS")? alias)? ;

subquery_factoring_clause :
	"WITH"
	{query_name "AS" "(" subquery ")", ","} ;

select_list :
	"*"
	| {   query_name "." "*"
	    | (schema ".")? (table | view) ".*"
	    | expression (("AS")? c_alias)?
	  , "," };

table_reference :
	(
	  only_reference_clause
	| "(" table_reference ")"
	| table column_list
	| "TABLE" "(" expression ")" (t_alias)?
	| query_table_expression (flashback_clause)? (t_alias)?
	)
	(joined_table_clause)? ;

joined_table_clause :
	  join_on_condition_clause
	| cross_join_clause
	| natural_join_clause
	;

join_on_condition_clause :
	(join_type)? "JOIN" table_reference
	("ON" condition | "USING" column_list );

cross_join_clause :
	"CROSS" "JOIN" ;

natural_join_clause :
	"NATURAL" (join_type)?
	"JOIN" table_reference ;

join_type :
	  "INNER"
	| ( "LEFT"
	  | "RIGHT"
	  | "FULL") ("OUTER")?;

flashback_clause :
	"AS" "OF" scn_or_timestamp
	expression ;

scn_or_timestamp :
	"SCN" | "TIMESTAMP" ;

only_reference_clause :
	"ONLY" query_table_expression
	(flashback_clause)? ;

query_table_expression :
	(schema ".")?
	  ( table ( (  "PARTITION" "(" partition ")"
        	       | "SUBPARTITION" "(" subpartition ")"
	            ) (sample_clause)?
	           | "@" dblink
        	     | sample_clause
        	  )?
	     | { view } ("@" dblink)?
	  )
	| "(" subquery (subquery_restriction_clause)? ")"
	| query_name
	;

sample_clause :
	"SAMPLE" (block_tag)? "(" sample_percent ")" ;

block_tag :
	"BLOCK" ;

subquery_restriction_clause :
	"WITH" ("READ" "ONLY"
		| "CHECK" "OPTION" ("CONSTRAINT" constraint)?) ;

table_collection_expression :
	"TABLE" "(" collection_expression ")"
	("(" "+" ")")? ;

hierarchical_query_clause :
	("START" "WITH" start_with_expression)?
	"CONNECT" "BY" connect_by_expression ;

start_with_expression :
	expression ;

connect_by_expression :
	expression ;

group_by_clause :
	"GROUP" "BY"
	{  rollup_cube_clause
	 | grouping_sets_clause
	 | expression
	 , ","}
	(having_clause)?;

rollup_cube_clause :
	("ROLLUP" | "CUBE")
	"(" grouping_expression_list ")";

grouping_sets_clause :
	"GROUPING" "SETS"
	"(" (rollup_cube_clause | grouping_expression_list) ")";

grouping_expression_list:
	{expression_list, ","} ;

order_by_clause :
	"ORDER" ("SIBLINGS")? "BY"
	{(expression | position | c_alias)
	 (ascending_or_descending)?
	 ("NULLS" "FIRST" | "NULLS" "LAST")?, ","} ;

for_update_clause :
	"FOR" "UPDATE"
	("OF" {qualified_name, ","})?
	( "NOWAIT" | "WAIT" integer )?;

select_into_clause :
	"INTO"
	({variable_name ("(" expression ")")? , ","} | record_name);

merge_statement :
	"MERGE" (hint)? "INTO"
	(schema ".")? table (t_alias)?
	"USING" (schema ".")?
	(table | view | "(" subquery ")" )
	(t_alias)? "ON" "(" condition ")"
	"WHEN" "MATCHED"
		"THEN" merge_update_clause
	"WHEN" "NOT" "MATCHED"
		"THEN" merge_insert_clause ;

merge_update_clause :
	"UPDATE" "SET"
	{column "=" (expression | default_tag), ","};

merge_insert_clause :
	"INSERT" column_list
	"VALUES" "(" {expression, ","} ")" ;

set_role :
	"SET" "ROLE"
	(   "ALL" ("EXCEPT" {role,","})?
	  | "NONE"
	  | {role ("IDENTIFIED" "BY" password)?, ","}
	)
	;

set_constraints :
	"SET" ("CONSTRAINT" | "CONSTRAINTS")
	("ALL" | {constraint, ","})

	("IMMEDIATE" | "DEFERRED");

rollback:
	"ROLLBACK" ("WORK")?
	( "TO" ("SAVEPOINT")? savepoint | "FORCE" string )?;

truncate_statement :
	"TRUNCATE"
	("TABLE" qualified_name
		(("PRESERVE" | "PURGE")
		  "MATERIALIZED" "VIEW" "LOG")?
	| "CLUSTER" qualified_name
	)
	(reuse_or_drop_storage)? ;

update_statement :
	"UPDATE" (hint)?
	(  dml_table_expression_clause
	 | "ONLY" "(" dml_table_expression_clause ")" )
	(t_alias)?
	update_set_clause
	(where_clause)?
	(returning_clause)?;



update_set_clause :
	"SET"
	(
	 {  column_list "=" "(" subquery ")"
	  | column "=" ( expression | "(" subquery ")" | default_tag ) , "," }
	  | "VALUE" "(" t_alias ")" "=" (expression | "(" subquery ")" )
	)
	;

where_clause :
	"WHERE" condition ;

variance:
	"VARIANCE" "(" (distinct_or_all)? expression ")"
	("OVER" "(" analytic_clause ")")? ;

var_pop:
	"VAR_POP" "(" expression ")"
	("OVER" "(" analytic_clause ")")? ;

var_samp:
	"VAR_SAMP" "(" expression ")"
	("OVER" "(" analytic_clause ")")? ;

vsize:
	"VSIZE" "(" expression ")" ;

width_bucket:
	"WIDTH_BUCKET"
	"(" expression "," min_value ","
		max_value "," num_buckets ")" ;


exception_declaration :
	exception_name "EXCEPTION" ;

exception_handler :
	"WHEN"
	( {exception_name, "OR"}
        | "OTHERS" )
	"THEN" {statement} ;

delete_statement :
	 "DELETE" ("FROM")? (table_reference
		 | ("THE")? "(" subquery ")" )
	 (alias)?
	 ("WHERE" (search_condition
		 | "CURRENT_OF" cursor_name))?
	 (returning_clause)?;

returning_clause :
	 ("RETURNING")?
	 ({single_row_expression, ","}
	 	"INTO" {bind_argument, ","}
	 |{multiple_row_expression, ","}
	 	"INTO" {bind_argument, ","} )?;

bind_argument :
	  variable_name
	| ":" host_variable_name
	| number
	;

hint :	// hints are contained in comments
	  ("/*+") *("*/") "*/"
	| ("--+") *(#eol) ;

insert_statement :
	"INSERT" (hint)?
	(single_table_insert | multi_table_insert);

single_table_insert :
	insert_into_clause
	(values_clause (insert_returning_clause)?
		| subquery | "(" subquery ")") ;

insert_into_clause :
	"INTO" dml_table_expression_clause (t_alias)?
	(column_list)? ;

values_clause :
	"VALUES" "(" { default_tag | expression, ","} ")" ;

insert_returning_clause :
	"RETURNING" {expression,","}
	"INTO" {bind_argument, ","};

multi_table_insert :
	("ALL" {insert_into_clause (values_clause)?}
	  | conditional_insert_clause
	)
	(subquery | "(" subquery ")") ;

conditional_insert_clause :
	("ALL" | "FIRST" )?
	{"WHEN" condition "THEN"
		{insert_into_clause (values_clause)?}}
	("ELSE" {insert_into_clause (values_clause)?})? ;


commit_statement :
	"COMMIT" ("WORK")?
	("COMMENT" squotedstring)? ;

rollback_statement :
	"ROLLBACK" ("WORK")?
	("TO" ("SAVEPOINT")? savepoint_name)?;

savepoint_statement :
	"SAVEPOINT" savepoint_name;

lock_table_statement :
	"LOCK" "TABLE" {table_reference, ","}
	"IN" lock_mode "MODE" ("NOWAIT")?;

lock_mode :
	  "ROW" ("SHARE" | "EXCLUSIVE")
	| "SHARE" ("UPDATE" | "ROW" "EXCLUSIVE")?
	| "EXCLUSIVE"
	;

noaudit_statement :
	"NOAUDIT"
	({sql_statement_clause, ","}
		| {schema_object_clause, ","})
	("WHENEVER" (not_tag)? "SUCCESSFUL")?;

associate_statistics :
	"ASSOCIATE" "STATISTICS" "WITH"
	(column_association | function_association) ;

column_association :
	"COLUMNS" {qualified_name, ","}
	using_statistics_type ;

function_association :
	associated_item {qualified_name, ","}
	( using_statistics_type
	| ( {default_cost_clause, ","}
	  | {default_selectivity_clause, ","}
	  )
	);

using_statistics_type :
	"USING" (qualified_name | "NULL") ;

default_cost_clause :
	"DEFAULT" "COST"
	"(" cpu_cost "," io_cost "," network_cost ")" ;

	cpu_cost : numeric ;
	io_cost : numeric ;
	network_cost : numeric ;

default_selectivity_clause :
	"DEFAULT" "SELECTIVITY" default_selectivity ;

	default_selectivity : numeric;

create_table :
	  relational_table
	| object_table
	| XMLType_table
	;

relational_table :
	"CREATE" (global_temporary)? "TABLE"
	(schema ".")? table ^"OF"
	("(" relational_properties ")")?
	(on_commit_clause)?
	({physical_properties | table_properties})?;

object_table :
	"CREATE" (global_temporary)? "TABLE"
	(schema ".")? table
	"OF" (schema ".")? object_type
	(object_table_substitution)?
	("(" object_properties ")")?
	(on_commit_clause)?
	(OID_clause)? (OID_index_clause)?
	(physical_properties)?
	(table_properties)?;

global_temporary :
	"GLOBAL" "TEMPORARY" ;

on_commit_clause :
	"ON" "COMMIT" delete_or_preserve "ROWS" ;

delete_or_preserve :
	  "DELETE"
	| "PRESERVE" ;

relational_properties :
	  {   column (datatype)? (default_tag expression)?
	      ({inline_constraint | inline_ref_constraint})?
	    | out_of_line_constraint
	    | out_of_line_ref_constraint
	    | supplemental_logging_props
	  , (",")?};

object_properties :
	{
		(column | attribute) (default_tag expression)?
		  ({inline_constraint} | inline_ref_constraint)?
		| out_of_line_constraint
		| out_of_line_ref_constraint
		| supplemental_logging_props
	, (",")?};

object_table_substitution :
	substitutable_clause ;

OID_clause :
	"OBJECT" plsql_identifier "IS"
	("SYSTEM" "GENERATED" | "PRIMARY" "KEY");

OID_index_clause :
	"OIDINDEX" (index)?
	"(" {physical_attributes_clause | tablespace_spec} ")" ;

tablespace_spec :
	"TABLESPACE" tablespace ;

physical_properties :
	{
	  segment_attributes_clause (data_segment_compression)?
	| organization_heap_clause
	| organization_index_clause
	| organization_external_clause
	| "CLUSTER" cluster column_list
	};

organization_heap_clause :
	"ORGANIZATION" "HEAP"
	(segment_attributes_clause)?
	(data_segment_compression)? ;

organization_index_clause :
	"ORGANIZATION" "INDEX"
	(segment_attributes_clause)?
	(index_org_table_clause)? ;

organization_external_clause :
	"ORGANIZATION" "EXTERNAL"
	external_table_clause ;

segment_attributes_clause :
	{  physical_attributes_clause
	 | tablespace_spec
	 | logging_clause
	} ;

physical_attributes_clause :
	{   pctfree_spec
	  | pctused_spec
	  | inittrans_spec
	  | maxtrans_spec
	  | storage_clause
	} ;

pctfree_spec :
	"PCTFREE" integer ;

pctused_spec :
	"PCTUSED" integer	;

inittrans_spec :
	"INITRANS" integer ;

maxtrans_spec :
	"MAXTRANS" integer ;

storage_clause :
	"STORAGE"
	("(")?
	 {  storage_initial_spec
	  | storage_next_spec
	  | minextents_spec
	  | maxextents_spec
	  | pctincrease_spec
	  | freelists_spec
	  | freelist_groups_spec
	  | storage_optimal_spec
	  | buffer_pool_spec
	  }
	(")")?;

storage_initial_spec :
	"INITIAL" integer (K_or_M)? ;

storage_next_spec :
	"NEXT" integer (K_or_M)? ;

minextents_spec :
	"MINEXTENTS" integer ;

maxextents_spec :
	"MAXEXTENTS" (integer | "UNLIMITED");

pctincrease_spec :
	"PCTINCREASE" integer ;

freelists_spec :
	"FREELISTS" integer ;

freelist_groups_spec :
	"FREELIST" "GROUPS" integer ;

storage_optimal_spec :
	"OPTIMAL" (integer (K_or_M)? | null_tag)? ;

buffer_pool_spec :
	"BUFFER_POOL" buffer_pool_option ;

buffer_pool_option :
	  "KEEP"
	| "RECYCLE"
	| "DEFAULT" ;

data_segment_compression :
	  "COMPRESS"
	| "NOCOMPRESS" ;

table_properties :
	(column_properties)?
	(table_partitioning_clauses)?
	(row_movement_clause)?
	(cache_clause)?
	(rowdependencies_spec)?
	(monitoring_spec)?
	(parallel_clause)?
	({ enable_disable_clause })?
	("AS" subquery)? ;

column_properties :
	{column_property} ;

column_property :
	  object_type_col_properties
	| nested_table_col_properties
	| (varray_col_properties | LOB_storage_clause)
	  (LOB_partition_storage)?
	| XMLType_column_properties
	;

object_type_col_properties :
	"COLUMN" column
	substitutable_column_clause ;

substitutable_column_clause :
	  ("ELEMENT")? "IS" "OF" ("TYPE")?
	  "(" ("ONLY")? type ")"
	| substitutable_clause ;

nested_table_col_properties :
	"NESTED" "TABLE"
	(nested_item | "COLUMN_VALUE")
	(substitutable_column_clause)?
	store_as_table_clause
	("(" ("(" object_properties ")")?
	(physical_properties)? (column_properties)? ")" )?
	("RETURN" "AS" ("LOCATOR" | "VALUE") )?;

store_as_table_clause :
	"STORE" "AS" storage_table ;

varray_col_properties :
	"VARRAY" varray_item
	(   substitutable_column_clause
	  | "STORE" "AS" "LOB"
	    (LOB_segname ("(" LOB_parameters ")")?
	      | "(" LOB_parameters ")")
	);

LOB_storage_clause :
	"LOB"
	(  "(" {LOB_item, ","} ")"
		"STORE" "AS" "(" LOB_parameters ")"
	 | "(" LOB_item ")"
		 "STORE" "AS"
	    	(LOB_segname ("(" LOB_parameters ")")?
		    	| "(" LOB_parameters ")")
	);

LOB_parameters :
	{ tablespace_spec
	| storage_in_row_clause
	| storage_clause
	| chunk_clause
	| pctversion_clause
	| retention_tag
	| freepools_clause
	| cache_clause
	};

storage_in_row_clause :
	enable_or_disable "STORAGE" "IN" "ROW" ;

logging_clause :
	  "LOGGING"
	| "NOLOGGING" ;

pctversion_clause :
	"PCTVERSION" integer ;

retention_tag :
	"RETENTION"	;

freepools_clause :
	"FREEPOOLS" integer ;

chunk_clause :
	"CHUNK" integer ;

LOB_partition_storage :
	"PARTITION" partition
	{LOB_storage_clause | varray_col_properties}
	("(" "SUBPARTITION" subpartition
	   {LOB_storage_clause | varray_col_properties}
	 ")"
	)?;

row_movement_clause :
	enable_or_disable "ROW" "MOVEMENT" ;

index_org_table_clause :
	{ mapping_table_clauses
	 | pctthreshold_clause
	 | key_compression
	 | index_org_overflow_clause
	}
	;

index_org_overflow_clause :
	("INCLUDING" column)?
	({segment_attributes_clause
	  | pctthreshold_clause})?
	"OVERFLOW"
	(segment_attributes_clause)?;

mapping_table_clause :
	  "MAPPING" "TABLE"
	| "NOMAPPING" ;

pctthreshold_clause :
	"PCTTHRESHOLD" integer ;

key_compression :
	  "COMPRESS" (integer)?
	| "NOCOMPRESS" ;

supplemental_logging_props :
	"SUPPLEMENTAL" "LOG" "GROUP"
	log_group column_list
	("ALWAYS")? ;

external_table_clause :
	"(" ("TYPE" access_driver_type)?
		external_data_properties ")"
	("REJECT" "LIMIT" (integer | "UNLIMITED"))? ;

external_data_properties :
	"DEFAULT" "DIRECTORY" directory
	("ACCESS" "PARAMETERS"
		("(" opaque_format_spec ")"
			| "USING" "CLOB" subquery))?
	"LOCATION" "(" {(directory ":")?
		location_specifier, ","} ")" ;

opaque_format_spec :
	  record_format_info
	| fields_clause ;

record_format_info :
	"RECORDS" record_size_spec
  	({ record_format_option })?
   	(fields_clause)?;

record_size_spec :
	  fixed_record_size
	| variable_record_size
	| delimited_record_size ;

fixed_record_size :
	"FIXED" integer ;

variable_record_size :
	"VARIABLE" integer ;

delimited_record_size :
	"DELIMITED" "BY" delimited_by_spec ;

delimited_by_spec :
	  "NEWLINE"
	| string ;

record_format_option :
	  record_format_characterset
	| record_format_endian
	| record_format_BOM
	| record_format_string_sizes
	| record_format_load_when
	| record_format_badfile
	| record_format_discardfile
	| record_format_logfile
	| record_format_readsize
	| record_format_date_cache
	| record_format_skip
   	;

record_format_characterset :
	"CHARACTERSET" string ;

record_format_endian :
	"DATA" "IS" little_or_big "ENDIAN" ;

little_or_big :
	  "LITTLE"
	| "BIG" ;

record_format_BOM :
	"BYTE" "ORDER" "MARK"
	check_or_nocheck ;

check_or_nocheck :
	  "CHECK"
	| "NOCHECK" ;

record_format_string_sizes :
	"STRING" "SIZES" "ARE" "IN"
	bytes_or_characters ;

bytes_or_characters :
	  "BYTES"
	| "CHARACTERS" ;

record_format_load_when :
	"LOAD" "WHEN" condition_spec ;

record_format_badfile :
	  "NOBADFILE"
	| "BADFILE"
		(directory_object_name ":" )? filename ;

record_format_discardfile :
	  "NODISCARDFILE"
	| "DISCARDFILE"
		(directory_object_name ":" )? filename ;

record_format_logfile :
	  "NOLOGFILE"
	| "LOGFILE"
		(directory_object_name ":" )? filename ;

record_format_readsize :
	"READSIZE" integer ;

record_format_date_cache :
	"DATE_CACHE" integer ;

record_format_skip :
	"SKIP" integer ;

fields_clause :
	"FIELDS" (delim_spec)? (trim_spec)?
	(missing_field_values_are_null)?
	(reject_rows_with_all_null_fields)?
	(field_list)? ;

missing_field_values_are_null :
	"MISSING" "FIELD" "VALUES" "ARE" "NULL" ;

reject_rows_with_all_null_fields :
	"REJECT" "ROWS" "WITH" "ALL" "NULL" "FIELDS" ;

position_spec :
	("POSITION")?
	"(" (pos_start | "*")
	    (("+" | "-") pos_increment)?
	    ":"
	    (pos_end | pos_length) ")" ;

	pos_start 	  : number;
	pos_increment : number;
	pos_end 	  : number;
	pos_length 	  : number;

delim_spec :
	  "ENCLOSED" "BY" string ("AND" string)?
	| "TERMINATED" "BY" (string | "WHITESPACE")
	  (("OPTIONALLY")? "ENCLOSED" "BY" string ("AND" string)?)? ;

trim_spec :
	  "LRTRIM"
	| "NOTRIM"
	| "LTRIM"
	| "RTRIM"
	| "LDRTRIM"
	;

field_list :
	"(" {field_name (position_spec)?
	(datatype)? (init_spec)?, ","} ")";


location_specifier : squotedstring ;

init_spec :
	("DEFAULTIF" | "NULLIF")
	condition;

table_partitioning_clauses :
	  range_partitioning
	| hash_partitioning
	| list_partitioning
	| composite_partitioning
	;

range_partitioning :
	"PARTITION" "BY" "RANGE" column_list
	  "(" {"PARTITION" (partition)? range_values_clause
	    table_partition_description, ","} ")" ;

hash_partitioning  :
	"PARTITION" "BY" "HASH" column_list
	(  individual_hash_partitions
	 | hash_partitions_by_quantity);

list_partitioning :
	"PARTITION" "BY" "LIST" "(" column ")"
	"(" {"PARTITION" (partition)? list_values_clause
		table_partition_description, ","} ")" ;

composite_partitioning :
	"PARTITION" "BY" "RANGE" column_list
	( subpartition_by_list | subpartition_by_hash )?
	  "(" {"PARTITION" (partition)? range_values_clause
	  	table_partition_description, ","} ")" ;

subpartition_by_hash :
	"SUBPARTITION" "BY" "HASH" column_list
	("SUBPARTITIONS" quantity (store_in_clause)?
	 | subpartition_template )?;

individual_hash_partitions :
	"(" {"PARTITION" (partition
		partitioning_storage_clause)?, ","} ")" ;

hash_partitions_by_quantity :
	"PARTITIONS" hash_partition_quantity
	(store_in_clause)?
	("OVERFLOW" store_in_clause)? ;

subpartition_by_list :
	"SUBPARTITION" "BY" "LIST" "(" column ")"
	(subpartition_template)? ;

subpartition_template :
	"SUBPARTITION" "TEMPLATE"
	"(" {SUBPARTITION subpartition
		(list_values_clause)?
		(partitioning_storage_clause)?, ","} ")"
	| hash_subpartition_quantity ;

range_values_clause :
	"VALUES" "LESS" "THAN"
	  "(" {value | "MAXVALUE", ","} ")" ;

list_values_clause :
	"VALUES" "("
	{ value
	| null_tag
	| default_tag, "," }
	")" ;

table_partition_description :
	(segment_attributes_clause)?
	(data_segment_compression)?
	(key_compression)?
	(table_partition_overflow_clause)?
	({ LOB_storage_clause | varray_col_properties })?
	(partition_level_subpartition)? ;

table_partition_overflow_clause :
	"OVERFLOW" (segment_attributes_clause)? ;

partition_level_subpartition :
	  "SUBPARTITIONS" hash_subpartition_quantity
	  (store_in_clause)?
	| "(" {subpartition_spec, ","} ")" ;

subpartition_spec :
	"SUBPARTITION" (subpartition)?
	(list_values_clause)?
	(partitioning_storage_clause)? ;

partitioning_storage_clause :
	( { tablespace_spec
	  | partitioning_storage_overflow_clause
	  | partitioning_storage_lob_clause
	  | partitioning_storage_varray_clause
	  }
	)? ;

partitioning_storage_overflow_clause :
	"OVERFLOW" (tablespace_spec)? ;

partitioning_storage_lob_clause  :
	"LOB" "(" LOB_item ")" "STORE" "AS"
	(LOB_segname ( "(" tablespace_spec ")" )?
		| "(" tablespace_spec ")" );

partitioning_storage_varray_clause :
	"VARRAY" varray_item
	"STORE" "AS" "LOB" LOB_segname ;

parallel_clause :
	  "NOPARALLEL"
	| "PARALLEL"
		(integer
		| "(" {plsql_identifier numeric, (",")?} ")")? ;

enable_disable_clause :
	enable_or_disable
	(validate_or_novalidate)?
	( unique_columns_spec
	| primary_key_spec
	| "CONSTRAINT" constraint
	)
	(using_index_clause)?
	(exceptions_clause)?
	(cascade_tag)?
	(keep_or_drop_index)? ;

unique_columns_spec :
	"UNIQUE" column_list ;

keep_or_drop_index :
	keep_or_drop "INDEX" ;

keep_or_drop :
	  "KEEP"
	| "DROP" ;

primary_key_spec :
	"PRIMARY" "KEY" ;

using_index_clause :
	"USING" "INDEX"
	( (schema ".")? index
		| "(" create_index ")"
		| "LOCAL"
		| global_partitioned_index
		| { pctfree_spec
		  | inittrans_spec
		  | maxtrans_spec
		  | tablespace_spec
		  | storage_clause
		  | "NOSORT"
		  | logging_clause
		  }
	)?;

global_partitioned_index :
	"GLOBAL" "PARTITION"
	"BY" "RANGE" column_list
	"(" {global_partitioning_clause, ","} ")" ;

global_partitioning_clause :
	"PARTITION" (partition)?
	"VALUES" "LESS" "THAN" "(" {value, ","} ")"
	  ( segment_attributes_clause )?;

create_tablespace :
	"CREATE" ("UNDO")? tablespace_spec
	(datafile_tempfile_clause)?
	({ minimum_extent_clause
	 | blocksize_clause
	 | logging_clause
	 | force_logging_spec
	 | default_storage_clause
	 | online_or_offline
	 | permanent_or_temporary
	 | extent_management_clause
	 | segment_management_clause
	 })? ;

permanent_or_temporary :
	  "PERMANENT"
	| "TEMPORARY" ;

extent_management_clause :
	"EXTENT" "MANAGEMENT"
	("DICTIONARY" | "LOCAL"
		("AUTOALLOCATE" | "UNIFORM" (size_spec)?)?);

segment_management_clause :
	"SEGMENT" "SPACE" "MANAGEMENT" ("MANUAL" | "AUTO");

minimum_extent_clause :
	"MINIMUM" "EXTENT" integer ( K_or_M )? ;

blocksize_clause :
	"BLOCKSIZE" integer ("K")? ;

create_temporary_tablespace :
	"CREATE" "TEMPORARY" tablespace_spec
	("TEMPFILE" datafile_tempfile_spec)?
	temp_tablespace_extent ;

temp_tablespace_extent :
	("EXTENT" "MANAGEMENT" "LOCAL")?
	("UNIFORM" ("SIZE" integer (K_or_M)?)?)? ;

create_trigger :
	"CREATE" ("OR" "REPLACE")?
	"TRIGGER" qualified_name
	("BEFORE" | "AFTER" | "INSTEAD" "OF")
	( dml_event_clause
	| {ddl_event, "OR"}
	| {database_event, "OR"}
	) ("ON" ((schema ".")? "SCHEMA" | "DATABASE"))?
	("WHEN" "(" condition ")")?
	(call_statement | plsql_block) ;

dml_event_clause :
	{"DELETE" | "INSERT" | "UPDATE" ("OF" {column, ","})?, "OR"}
	"ON" (qualified_name |
		("NESTED" "TABLE" nested_table_column "OF")? qualified_name)
	(referencing_clause)? ("FOR" "EACH" "ROW")? ;

ddl_event :
 	  "ALTER"   | "ANALYZE" | "ASSOCIATE" "STATISTICS"    | "AUDIT"
	| "COMMENT" | "CREATE"  | "DISASSOCIATE" "STATISTICS" | "DROP"
	| "GRANT"   | "NOAUDIT" | "RENAME" | "REVOKE" | "TRUNCATE"
	| "DDL" ;

database_event :
	  "SERVERERROR" | "LOGON" | "LOGOFF" | "STARTUP"
	| "SHUTDOWN" | "SUSPEND" ;

nested_table_column : plsql_identifier;

referencing_clause :
	"REFERENCING"
	{ "OLD" ("AS")? old
	| "NEW" ("AS")? new
	| "PARENT" ("AS")? parent } ;

	old 	 : plsql_identifier;
	new 	 : plsql_identifier;
	parent : plsql_identifier;




call_statement :
	"CALL"
	(object_access_expression
		| qualified_name ("@" dblink)?
	)
	"(" {expression, ","} ")"
	("." simple_expression)?
	("INTO" ":" host_variable
		(("INDICATOR")? ":" indicator_variable)?)?;

create_cluster :
	"CREATE" "CLUSTER" cluster_name
	"(" {column datatype, ","} ")"
	({ physical_attributes_clause
	 | size_spec
	 | tablespace_spec
	 | ("INDEX" | ("SINGLE" "TABLE")?
	 	"HASHKEYS" integer ("HASH" "IS" expression)?)
	 })?
	(parallel_clause)?
	(rowdependencies_spec)?
	(cache_clause)?;

cluster_name :
	qualified_name ;

create_type :
	"CREATE" ("OR" "REPLACE")? "TYPE"
	(schema ".")? type_name		// incomplete type stops here...
	(  create_varray_type
	 | create_nested_table_type
	 | create_object_type
	)?;

create_varray_type :
	("IS" | "AS")
	("VARRAY" | "VARYING" "ARRAY")
	"(" limit ")" "OF" datatype ;

create_nested_table_type :
	("IS" | "AS")
	"TABLE" "OF" datatype ;

create_object_type :
	(invoker_rights_clause)?
	(("IS" | "AS") "OBJECT"
		| "UNDER" (schema ".")? supertype)
	(sqlj_object_type)?
	("(" {attribute_spec | element_spec, ","} ")" )?
	((not_tag)? final_tag)?
	((not_tag)? instantiable_tag)? ;

invoker_rights_clause :
	"AUTHID" ("CURRENT_USER" | "DEFINER");

attribute_spec :
	attribute
	datatype
	(sqlj_object_type_attr)? ;

SQLData :
	"SQLData" "(" ({sql_data_item, ","})? ")"
	((not_tag)? final_tag)? ;

sql_data_item :
	(parameter datatype | subprogram_clauses)
	(sqlj_object_type_sig)?
      ;

element_spec :
	(inheritance_clauses)?
	{  subprogram_clauses
 	 | constructor_spec
	 | map_order_function_spec }
	("," {pragma_clause, ","})? ;

inheritance_clauses :
	(not_tag)?
	( overriding_tag
	| final_tag
	| instantiable_tag );

subprogram_clauses :
	member_or_static
	(procedure_spec | function_spec);

member_or_static :
	  "MEMBER"
	| "STATIC" ;

procedure_spec :
	"PROCEDURE" name
	("(" arguments ")")?
	( ("IS" | "AS") call_spec )? ;

function_spec :
	"FUNCTION" name
	("(" arguments ")")?
	return_clause
	("DETERMINISTIC")? ;



constructor_spec :
	(final_tag)? (instantiable_tag)?
	"CONSTRUCTOR" "FUNCTION" datatype
	( ("SELF" "IN" "OUT" datatype ",")?
		parameter_list)?
	return_clause ;

map_order_function_spec :
	map_or_order
	"MEMBER" function_spec ;

map_or_order :
	  "MAP"
	| "ORDER" ;

return_clause :
	  "RETURN" (self_as_result | datatype)
	  ( ("IS" | "AS") call_spec )?
	  (sqlj_object_type_sig)? ;

self_as_result :
	"SELF" "AS" "RESULT" ;

pragma_clause :
	"PRAGMA" pragma_name
	("(" (method_name | default_tag) ","
	    {pragma_arg, ","}
	")")? ;

pragma_arg :
	  "RNDS"
	| "WNDS"
	| "RNPS"
	| "WNPS"
	| "TRUST"
	| number
	;

pragma_name : identifier;

call_spec :
	"LANGUAGE" (Java_declaration | C_declaration);

Java_declaration :
	"JAVA" "NAME" squotedstring ;

C_declaration :
	"C" ("NAME" name)?
	"LIBRARY" lib_name
	("AGENT" "IN" "(" argument ")")?
	("WITH" "CONTEXT")?
	("PARAMETERS" "(" parameters ")")? ;


create_type_body :
	"CREATE" ("OR" "REPLACE")?
	"TYPE" "BODY" (schema ".")? type_name
	("IS" | "AS")
	({
		member_or_static
	      (  procedure_declaration
	       | function_declaration
	       | constructor_declaration )
	   |  map_or_order "MEMBER" function_declaration
	})?
	"END" ;

procedure_declaration :
	"PROCEDURE" name
	("(" (arguments)? ")")?
	("IS" | "AS")
	(plsql_block | call_spec)
	(";")?;

function_declaration :
	"FUNCTION" name
	("(" (arguments)? ")")?
	"RETURN" datatype
	("IS" | "AS")
	(plsql_block | call_spec)
	(";")?;

constructor_declaration :
	(final_tag)? (instantiable_tag)?
	"CONSTRUCTOR" "FUNCTION" datatype
	( ("SELF" "IN" "OUT" datatype "," )?
	parameter_list)?
	"RETURN" self_as_result
	("IS" | "AS") (plsql_block | call_spec)
	(";")?;

parameter_list :
	{parameter datatype, ","} ;

create_package :
	"CREATE" ("OR" "REPLACE")?
	"PACKAGE" (schema ".")? package
	(invoker_rights_clause)? ("IS" | "AS")
	plsql_package_spec
	"END" (label_name)? ;

plsql_package_spec :
	{  procedure_spec (";")?
	 | function_spec (";")?
	 | type_definition (";")?
	 | item_definition (";")?
	 | exception_declaration (";")?
	 | pragma_clause (";")?
	} ;

create_package_body :
	"CREATE" ("OR" "REPLACE")?
	"PACKAGE" "BODY" (schema ".")? package
	("IS" | "AS")
	plsql_package_body;

plsql_package_body :
	("<<" label_name ">>")?
	("DECLARE")?
	({type_definition | item_definition})?
	({function_declaration | procedure_declaration})?
	("BEGIN"
	  ({statement})?)?
	"END" (label_name)? (";")? ;

create_procedure :
	"CREATE" ("OR" "REPLACE")?
	"PROCEDURE" (schema ".")? procedure
	("(" arguments ")" )?
	(invoker_rights_clause)?
	("IS" | "AS")
	(plsql_subprogram_body | call_spec) ;

create_profile :
	"CREATE" "PROFILE" profile_name "LIMIT"
	{resource_parameter | password_parameter};

create_role :
	"CREATE" "ROLE" role
	(role_identity_clause)? ;

create_user :
	"CREATE" "USER" user_name
	({create_user_option})? ;

create_user_option :
	  user_identified_by_clause
	| default_tablespace_clause
	| temporary_tablespace_clause
	| quota_clause
	| profile_clause
	| default_role_clause
	| password_expire_clause
	| account_lock_clause
	| proxy_clause
	;

user_identified_by_clause :
	"IDENTIFIED"
	( "BY" password ("REPLACE" old_password)?
	| externally_or_globally
	) ;

default_tablespace_clause :
	"DEFAULT" tablespace_spec ;

temporary_tablespace_clause :
	"TEMPORARY" tablespace_spec ;

profile_clause :
	"PROFILE" profile_name ;

default_role_clause :
	"DEFAULT" "ROLE"
	( {role, ","}
		| "ALL" ( "EXCEPT" {role,","} )?
	      | "NONE"
	) ;

password_expire_clause :
	"PASSWORD" "EXPIRE" ;

account_lock_clause :
	"ACCOUNT" lock_or_unlock ;

lock_or_unlock :
	"LOCK" | "UNLOCK" ;

quota_clause :
	"QUOTA"
	(integer ( K_or_M )? | "UNLIMITED")
	"ON" tablespace ;

create_rollback_segment :
	"CREATE" ("PUBLIC")?
	"ROLLBACK" "SEGMENT" rollback_segment
	({ tablespace_spec | storage_clause })?;

create_schema :
	"CREATE" "SCHEMA" "AUTHORIZATION" schema
	{ create_table | create_view | grant_statement };

create_view :
	"CREATE" ("OR" "REPLACE")?
	(("NO")? "FORCE")? "VIEW" (schema ".")? view
	(view_clause)?
	"AS" subquery (subquery_restriction_clause)?;

view_clause :
	"(" {view_alias ({inline_constraint})?
		| out_of_line_constraint, ","} ")"
	| object_view_clause
	| XMLType_view_clause
	;

view_alias :
	identifier (? #value !: "CONSTRAINT" );

object_view_clause :
	"OF" qualified_name
	(with_object_id_clause
	 | "UNDER" qualified_name)
	("(" {out_of_line_constraint
		| attribute {inline_constraint}, ","} ")")?
	;

with_object_id_clause :
	"WITH" "OBJECT" ("IDENTIFIER" | "ID")
	(default_tag | "(" {attribute, ","}  ")") ;

create_materialized_view :
	"CREATE" "MATERIALIZED" "VIEW"
	(schema ".")? materialized_view
	("OF" (schema ".")? object_type)?
  	("(" scoped_table_ref_constraint ")")?
  	({"ON" "PREBUILT" "TABLE"
  		(("WITH" | "WITHOUT") "REDUCED" "PRECISION")?
	 | physical_properties
	 | materialized_view_props })?
	("USING" "INDEX"
	    ({physical_attributes_clause | tablespace_spec})?
  	 | "USING" "NO" "INDEX")?
  	(mv_refresh)?
  	("FOR" "UPDATE")?
  	(("DISABLE" | "ENABLE") "QUERY" "REWRITE")?
  	"AS" subquery;

ref_column 		: plsql_identifier;
ref_attribute 	: plsql_identifier;
scope_table_name 	: plsql_identifier;

scoped_table_ref_constraint :
	{"SCOPE" "FOR" "(" (ref_column | ref_attribute) ")"
	  "IS" (schema ".")? scope_table_name, ","} ;

materialized_view_props :
  	{ column_properties
	| table_partitioning_clauses
	| cache_or_nocache
	| parallel_clause
	| build_clause
	};

cache_or_nocache :
	  "CACHE"
	| "NOCACHE" ;

build_clause :
	"BUILD" ("IMMEDIATE" | "DEFERRED");

mv_refresh :
	"REFRESH"
	{
		("FAST" | "COMPLETE" | "FORCE")
	  	| "ON" ("DEMAND" | "COMMIT")
		| ("START" "WITH" | "NEXT") date
		| "WITH" (primary_key_spec | "ROWID")
		| "USING" ("DEFAULT" ("MASTER" | "LOCAL")? "ROLLBACK" "SEGMENT"
	    	| ("MASTER" | "LOCAL")? "ROLLBACK" "SEGMENT" rollback_segment
	    )
	    ({"DEFAULT" ("MASTER" | "LOCAL")? "ROLLBACK" "SEGMENT"
	    	| ("MASTER" | "LOCAL")? "ROLLBACK" "SEGMENT" rollback_segment
	    })?
	}
	| "NEVER" "REFRESH"
	;

create_materialized_view_log :
	"CREATE" "MATERIALIZED" "VIEW" "LOG" "ON" (schema ".")? table
	({ physical_attributes_clause
	 | tablespace_spec
	 | logging_clause
	 | cache_or_nocache
	})?
	(parallel_clause)?
	(table_partitioning_clauses)?
	("WITH"
	 { "OBJECT" "ID"
	  | primary_key_spec
	  | "ROWID"
	  | "SEQUENCE"
	  | column_list
	 , (",")? }
	 ( new_values_clause )?
	)?;

new_values_clause :
	("INCLUDING" | "EXCLUDING") "NEW" "VALUES" ;

grant_statement :
	"GRANT"
	{ grant_system_privileges
	| grant_object_privileges
	} ;

grant_system_privileges :
	{system_privilege | role | "ALL" "PRIVILEGES", ","}
	"TO" grantee_clause ("IDENTIFIED" "BY" password)?
	("WITH" "ADMIN" "OPTION")? ;

grant_object_privileges :
	{(object_privilege | "ALL" ("PRIVILEGES")?)
		(column_list)?, ","}
	"ON" on_object_clause
	"TO" grantee_clause
	("WITH" "GRANT" "OPTION")?
	("WITH" "HIERARCHY" "OPTION")? ;

grantee_clause :
	{user_name | role | "PUBLIC", ","} ;

object_privilege :
	  sql_verb
	| "REFERENCES"
	;

on_object_clause :
	  "DIRECTORY" directory_name
	| "JAVA" ("SOURCE" | "RESOURCE") qualified_name
	| (schema ".")? object_name
	;

set_transaction_statement :
	"SET" "TRANSACTION"
	( (read_write_clause
	  | "ISOLATION" "LEVEL"
		  ("SERIALIZABLE" | "READ" "COMMITTED")
	  | "USE" "ROLLBACK" "SEGMENT" rollback_segment
	  )
	  ("NAME" string)?
	| "NAME" string
	) ;

revoke_statement :
	"REVOKE"
	{ revoke_system_privileges
	| revoke_object_privileges, ","}
	;

revoke_system_privileges :
	{  system_privilege
	 | role
	 | "ALL" "PRIVILEGES", "," }
	"FROM" grantee_clause ;

revoke_object_privileges :
	{(  object_privilege
	  | "ALL" ("PRIVILEGES")?)
	  (column_list)?, ","}
	"ON" on_object_clause
	"FROM" grantee_clause
	(cascade_constraints_tag)?
	(force_tag)?;

create_snapshot :
	"CREATE" "SNAPSHOT" ("LOG" "ON")? snapshot_name
	({ pctfree_spec
  	 | pctused_spec
  	 | tablespace_spec
  	 | "WITH" {"ROWID" | primary_key_spec | "(" column ")", ","}
  	 | refresh_clause
  	 | storage_clause
  	 | using_index_clause
  	})?
  	("AS" subquery)? ;

refresh_clause :
	"REFRESH"
	({ "FAST"
	 | "NEXT" expression
	 | "COMPLETE"
	 | "WITH" "PRIMARY" "KEY"
	 | ("USING")? ("DEFAULT")? ("MASTER" | "LOCAL")?
	   "ROLLBACK" "SEGMENT" (segment_name)?
	 | "START" "WITH" expression "NEXT" expression
	})?
	;

create_function :
	"CREATE" ("OR" "REPLACE")?
	"FUNCTION" function_name
	("(" arguments ")")?
	"RETURN" datatype

	({  invoker_rights_clause
	 | "DETERMINISTIC"
	 | parallel_enable_clause })?
	( ("AGGREGATE" | "PIPELINED")
	   "USING" implementation_type
	  | ("PIPELINED")? ("IS" | "AS")
	   (plsql_function_body | call_spec)
	)
	;

parallel_enable_clause :
	"PARALLEL_ENABLE"
	("(" "PARTITION" argument "BY"
	   ("ANY" | ("HASH" | "RANGE") column_list)
	 ")"
	 (streaming_clause)?
	)?;

streaming_clause :
	("ORDER" | "CLUSTER") "BY" column_list ;

arguments :
	{argument, ","};

argument :
	plsql_identifier
	(in_out_spec)?
	(nocopy_spec)?
	datatype
	(argument_default_clause)? ;

in_out_spec :
	  "IN" "OUT"
	| "IN"
	| "OUT" ;

nocopy_spec :
	"NOCOPY" ;

argument_default_clause :
	"DEFAULT"
	expression ;

plsql_block :
	("<<" label_name ">>")?
	("DECLARE")?
	({ type_definition
	 | item_definition
	 | pragma_clause (";")? })?
	({function_declaration | procedure_declaration})?
	"BEGIN"
		({statement})?
		("EXCEPTION" {exception_handler})?
	"END" (label_name)? (";")?

	;

item_definition :
	item_declaration (";")? ;

ref_cursor_type_definition :
	"TYPE" identifier "IS" "REF"

	cursor_declaration ;
type_definition :
	(
	  record_type_definition
	| ref_cursor_type_definition
	| table_type_definition
	| subtype_definition
	| varray_type_definition
	) ";"
	;

subtype_definition :
	"SUBTYPE" subtype_name "IS" base_type
	("(" constraint ")")? (not_null_spec)?;

constant_declaration :
	constant_name "CONSTANT" datatype
	(initializer)? ;

initializer :
	(not_null_spec)?
	(":=" | "=" | default_tag) expression ;

not_null_spec :
	  "NOT_NULL"
	| "NOT" "NULL" ;

item_declaration :
	  variable_declaration		// subsumes object_declaration
	| collection_declaration
	| constant_declaration
	| cursor_declaration
	| exception_declaration
	| record_declaration
	;

table_type_definition :
	"TYPE" type_name "IS"
	"TABLE" "OF" element_type
	(not_null_spec)?
	("INDEX" "BY" "BINARY_INTEGER")?;

varray_type_definition :
	"TYPE" type_name "IS"
	("VARRAY" | "VARRYING ARRAY") "(" size_limit ")"
	"OF" element_type
	(not_null_spec)? ;

collection_declaration :
   collection_name type_name ;

element_type :
	  datatype
	| cursor_name "%ROWTYPE"
	| db_table_name ( "%ROWTYPE" | "." column "%TYPE")
	| object_name "%TYPE"
	| ("REF")? object_type_name
	| record_name ("." field_name)? "%TYPE"
	| record_type_name
	| scalar_datatype_name
	| variable_name "%TYPE"
	;

object_type_declaration :
	"CREATE" ("OR_REPLACE")?
	"TYPE" (schema_name ".")? type_name
	 ("AUTHID" ("CURRENT_USER" | "DEFINER"))?
	 ("IS" | "AS") "OBJECT" "(" member_list ")" ;

member_list :
	{attribute_name attribute_type, ","} ;

object_type_body :
	"CREATE" ("OR_REPLACE")?
	"TYPE_BODY" (schema_name ".")? type_name
	("IS" | "AS")
	{member_or_static (subprogram_body | call_spec) ";"}
	(map_or_order "MEMBER" function_body ";")?
	"END" ;

record_type_definition :
	"TYPE" type_name "IS RECORD"
	"(" {field_declaration, ","} ")" ;

field_declaration :
	field_name datatype
	(initializer)? ;

record_declaration :
	record_name type_name;

variable_declaration :
	{variable_name, ","} datatype
	(initializer)? ;

cursor_declaration :
	"CURSOR" (cursor_name)?
	("(" {cursor_parameter_declaration, ","}")")?
	("RETURN" rowtype)?
	("IS" select_statement)?;

cursor_body :
	"CURSOR" cursor_name
	("(" {cursor_parameter_declaration, ","} ")")?
	"RETURN" rowtype "IS" select_statement;

cursor_parameter_declaration :
	parameter_name ("IN")? datatype
	(initializer)? ;

rowtype :
	((  db_table_name
	  | cursor_name
	  | cursor_variable_name ) "%" "ROWTYPE"
	 | record_name "%" "TYPE"
	 | record_type_name) ;

column_list :
	"(" {column, ","} ")" ;

condition : expression ;

rename_statement :
	"RENAME" old_name "TO" new_name;

create_operator :
	"CREATE" ("OR" "REPLACE")? "OPERATOR"
	qualified_name binding_clause;

binding_clause :
	"BINDING"
	{"(" {parameter_type, ","} ")"
		"RETURN" return_type  implementation_clause, ","} ;

	parameter_type : datatype;
	return_type : datatype;

implementation_clause :
	("ANCILLARY" "TO"
	  {primary_operator "(" {parameter_type, ","} ")", ","}
	| context_clause ("COMPUTE" "ANCILLARY" "DATA")?
	)?
	using_function_clause ;

	primary_operator : plsql_identifier;

context_clause :
	"WITH" "INDEX" "CONTEXT" ","
	"SCAN" "CONTEXT" implementation_type ;

using_function_clause :
	"USING" qualified_name ;

create_context :
	"CREATE" ("OR" "REPLACE")? "CONTEXT"
	namespace "USING" qualified_name
	("INITIALIZED" externally_or_globally
	| "ACCESSED" "GLOBALLY")?
	;

	namespace : identifier;

create_controlfile :
	"CREATE" "CONTROLFILE" (reuse_tag)?
	("SET")? "DATABASE" database
	({
	   logfile_clause
	 | resetlogs_spec
	 | datafile_tempfile_clause
	 | maxlogfiles_spec
	 | maxlogmembers_spec
	 | maxloghistory_spec
	 | maxdatafiles_spec
	 | maxinstances_spec
	 | archivelog_spec
	 | force_logging_spec
	 })?
	(character_set_clause)?;

maxlogfiles_spec :
	"MAXLOGFILES" integer ;

maxlogmembers_spec :
	"MAXLOGMEMBERS" integer ;

maxloghistory_spec :
	"MAXLOGHISTORY" integer ;

maxdatafiles_spec :
	"MAXDATAFILES" integer ;

maxinstances_spec :
	"MAXINSTANCES" integer ;

logfile_clause :
	"LOGFILE" {(logfile_group_spec)? redo_log_file_spec, ","} ;

logfile_group_spec :
	"GROUP" integer ;

force_logging_spec :
	("NO")? "FORCE" "LOGGING" ;

character_set_clause :
	("NATIONAL")? "CHARACTER" "SET" character_set ;

create_index :
	"CREATE" ("UNIQUE" | "BITMAP")? "INDEX"
	(schema ".")? index "ON"
	(  cluster_index_clause
	 | table_index_clause
	 | bitmap_join_index_clause);

cluster_index_clause :
	"CLUSTER" (schema ".")? cluster index_attributes ;

table_index_clause :
	(schema ".")? table (t_alias)?
	"(" {index_expr (ascending_or_descending)?, ","} ")"
	({   global_partitioned_index
	  | local_partitioned_index
	  | index_attributes
	  | domain_index_clause
	})?;

bitmap_join_index_clause :
	(schema ".")? table
	"(" {(schema ".")? (table)?
		column (ascending_or_descending)?, ","} ")"
	from_clause
	where_clause
	(local_partitioned_index)?
	index_attributes ;

index_expr :
	expression ;

index_attributes :
	({ physical_attributes_clause
	 | logging_clause
	 | online_tag
	 | compute_statistics_tag
	 | "TABLESPACE" (tablespace | default_tag)
	 | key_compression
	 | "NOSORT"
	 | "REVERSE"
	 | parallel_clause
	 })?;

local_partitioned_index :
	"LOCAL"
	(
	  on_comp_partitioned_table
	| on_range_partitioned_table
	| on_list_partitioned_table
	| on_hash_partitioned_table
	)?;

domain_index_clause :
	"INDEXTYPE" "IS" indextype
	(parallel_clause)?
	("PARAMETERS" "(" squotedstring ")")? ;

on_range_partitioned_table :
	"(" {
		"PARTITION" (partition
			({segment_attributes_clause})? )?
	    , "," }
	")" ;

on_list_partitioned_table :
	"(" {
		"PARTITION" (partition
			({segment_attributes_clause})? )?
	    , "," }
	")" ;

on_hash_partitioned_table :
	  store_in_clause
	| "(" {"PARTITION" (partition
		(tablespace_spec)?)?, ","} ")"
	;

on_comp_partitioned_table :
	(store_in_clause)?
	"(" {"PARTITION"
	   (partition ({segment_attribute_clause})?
	   (index_subpartition_clause)?)?, ","}
	 ")";

index_subpartition_clause :
	  store_in_clause
	| "(" {"SUBPARTITION" (subpartition
		(tablespace_spec)?)?, ","} ")" ;

store_in_clause :
	"STORE" "IN" "(" ({tablespace, ","} | default_tag) ")" ;

create_indextype :
	"CREATE" ("OR" "REPLACE")? "INDEXTYPE"
	indextype_name "FOR"
	{(schema ".")? operator_name "(" {paramater_type, ","} ")", ","}
	"USING" implementation_type;

	indextype_name : qualified_name ;
	paramater_type : datatype;


create_database :
	"CREATE" "DATABASE" (database)?
	({ "USER" ("SYS" | "SYSTEM")
		"IDENTIFIED" "BY" password
	| "CONTROLFILE" "REUSE"
	| "LOGFILE" {(logfile_group_spec)? redo_log_file_spec, ","}
	| maxlogfiles_spec
	| maxlogmembers_spec
	| maxloghistory_spec
	| maxdatafiles_spec
	| maxinstances_spec
	| archivelog_spec
	| force_logging_spec
	| character_set_clause
	| datafile_tempfile_clause
	| extent_management_local_spec
	| default_temp_tablespace
	| undo_tablespace_clause
	| set_time_zone_clause
	})? ;

default_temp_tablespace :
	"DEFAULT" "TEMPORARY"
	tablespace_spec
	("TEMPFILE" datafile_tempfile_spec)?
	temp_tablespace_extent_clause ;

temp_tablespace_extent_clause :
	(extent_management_local_spec)?
	("UNIFORM" (size_spec)?)? ;

extent_management_local_spec :
	"EXTENT" "MANAGEMENT" "LOCAL"	;

undo_tablespace_clause :
	"UNDO" tablespace_spec
	(datafile_tempfile_clause)? ;

create_database_link :
	"CREATE" ("SHARED")? ("PUBLIC")?
	"DATABASE" "LINK" dblink
	("CONNECT" "TO"
		( "CURRENT_USER"
		| user_name "IDENTIFIED"
		  "BY" password (authenticated_clause)?)
	| authenticated_clause
	)?
	("USING" connect_string)?;

connect_string : string;

authenticated_clause :
	"AUTHENTICATED" "BY" user_name
	"IDENTIFIED" "BY" password ;

create_dimension :
	"CREATE" "DIMENSION" qualified_name  	{level_clause}
	{hierarchy_clause | attribute_clause} ;

create_directory :
	"CREATE" ("OR" "REPLACE")? "DIRECTORY"
	 directory "AS" path_name;

	 path_name : string;

create_library :
	"CREATE" ("OR" "REPLACE")?
	"LIBRARY" qualified_name
	("IS" | "AS") filename
	("AGENT" agent_dblink)?;

create_outline :
	"CREATE" ("OR" "REPLACE")?
	("PUBLIC" | "PRIVATE")?
	"OUTLINE" (outline)?
	("FROM" ("PUBLIC" | "PRIVATE" )? source_outline)?
	("FOR" "CATEGORY" category)?
	("ON" statement)? ;

	source_outline : plsql_identifier;
	category : plsql_identifier;

agent_dblink : string;

create_synonym :
	"CREATE" ("OR" "REPLACE")? ("PUBLIC")?
	"SYNONYM" synonym_name
	"FOR" qualified_name ("@" dblink)?;

synonym_name : qualified_name ;

create_pfile :
	"CREATE" "PFILE" ("=" pfile_name)?
	"FROM" "SPFILE" ("=" spfile_name)?;

	pfile_name : string;
	spfile_name : string;

alter_database_statement :
	"ALTER" "DATABASE" (database)?
	( startup_clauses
	| recovery_clauses
	| database_file_clauses
	| logfile_clauses
	| controlfile_clauses
	| standby_database_clauses
	| default_settings_clauses
	| conversion_clauses
	| redo_thread_clauses
	| security_clause
	) ;

startup_clauses :
	  startup_mount_clause
	| startup_open_clause
	;

startup_mount_clause :
	"MOUNT" (startup_mount_option "DATABASE")? ;

startup_mount_option :
	  "STANDBY"
	| "CLONE" ;

startup_open_clause :
	"OPEN"
	read_write_clause
	(resetlogs_spec)?
	(migrate_clause)? ;

migrate_clause :
	"MIGRATE" ;

recovery_clauses :
	  general_recovery
	| managed_standby_recovery
	| end_backup_tag
	;

general_recovery :
	"RECOVER" ("AUTOMATIC")? ("FROM" location)?
	(( full_database_recovery
	  | partial_database_recovery
	  | "LOGFILE" filename
	  )
	  ({ "TEST"
	   | "ALLOW" integer "CORRUPTION"
	   | parallel_clause})?
	| "CONTINUE" (default_tag)?
	| "CANCEL"
	);

location : string;

full_database_recovery :
	((("STANDBY")? "DATABASE")?
	({"UNTIL"
		(  "CANCEL"
		 | "TIME" date
		 | "CHANGE" integer )
	 | "USING" "BACKUP" "CONTROLFILE"
	})?) <1,> ;

	date 	     : expression;
	filenumber : numeric;

datafile_tempfile_clause :
	"DATAFILE" {datafile_tempfile_spec, (",")?} ;

datafile_tempfile_spec :
	string
	({  size_spec
	  | reuse_tag
	  | autoextend_clause
	})?
	| autoextend_clause ;

redo_log_file_spec :
	( log_file_name | "(" {log_file_name, ","} ")")
	(size_spec)? ;

log_file_name :
	string ;

partial_database_recovery :
	  "TABLESPACE" {tablespace, ","}
	| datafile_filename_number_clause
	| "STANDBY"
		("TABLESPACE" {tablespace, ","}
		| datafile_filename_number_clause
		"UNTIL" ("CONSISTENT" "WITH")? "CONTROLFILE")
	;

datafile_filename_number_clause :
	"DATAFILE" ({filename, ","} | {filenumber, ","}) ;

managed_standby_recovery :
	"RECOVER" "MANAGED" "STANDBY" "DATABASE"
	  (recover_clause | cancel_clause | finish_clause)?;

recover_clause :
	{
	  recover_disconnect_clause
	| recover_timeout_clause
	| recover_delay_clause
	| recover_expire_clause
	| recover_next_clause
	| recover_through_clause
	| parallel_clause
	} ;

recover_disconnect_clause :
	"DISCONNECT" (from_session)? ;

recover_delay_clause :
	  "NODELAY"
	| "DEFAULT" "DELAY"
	| "DELAY" integer ;

recover_timeout_clause :
	  "TIMEOUT" integer
	| "NOTIMEOUT" ;

recover_expire_clause :
	  "EXPIRE" integer
	| "NO" "EXPIRE" ;

recover_next_clause :
	"NEXT" integer ;

recover_through_clause :
	"THROUGH"
	(( "THREAD" integer )? "SEQUENCE" integer
	  | "ALL" "ARCHIVELOG"
	  | ("ALL" | "LAST" | "NEXT") "SWITCHOVER"
	);

cancel_clause :
	"CANCEL" ("IMMEDIATE")? ("NOWAIT")?;

finish_clause :
	("DISCONNECT" (from_session)?)?
	(parallel_clause)?
	"FINISH" ("SKIP" ("STANDBY" "LOGFILE")?)?
	("WAIT" | "NOWAIT")? ;

database_file_clauses :
	  "CREATE" "DATAFILE"
	  ({filename,","} | {filenumber,","}) 	  ("AS" ({datafile_tempfile_spec,","} |"NEW"))?
	| datafile_filename_number_clause
	  ( online_or_offline
	  | resize_clause
	  | autoextend_clause
	  | end_backup_tag
	  )
	| "TEMPFILE" ({filename,","} | {filenumber,","})
	  ( resize_clause
	  | autoextend_clause
	  | "DROP" ("INCLUDING" "DATAFILES")?
	  | online_or_offline
	  )
	| "RENAME" "FILE" {filename,","} "TO" filename
	;

resize_clause :
	"RESIZE" integer (K_or_M)? ;

autoextend_clause :
	"AUTOEXTEND" ("OFF" | "ON"
		("NEXT" integer (K_or_M)?)?
	(maxsize_clause)?);

maxsize_clause :
	"MAXSIZE" ("UNLIMITED" | integer (K_or_M)?);

logfile_clauses :
    	  archivelog_spec
	| force_logging_spec
	| "ADD" ("STANDBY")? "LOGFILE" ("THREAD" integer)?
	  {(logfile_group_spec)? redo_log_file_spec, ","}
	  (size_spec)?
	| "DROP" ("STANDBY")? "LOGFILE" {logfile_descriptor,","}
	| "ADD" ("STANDBY")? "LOGFILE" "MEMBER" {filename (reuse_tag)?,","}
	  "TO" {logfile_descriptor,","}
	| "DROP" ("STANDBY")? "LOGFILE" "MEMBER" {filename, ","}
	| "ADD" "SUPPLEMENTAL" "LOG" "DATA"
	   ( "(" {primary_key_spec | "UNIQUE" "INDEX", ","} ")" "COLUMNS" )?
	| "DROP" "SUPPLEMENTAL" "LOG" "DATA"
	| "RENAME" "FILE" {filename, ","} "TO" filename
	| "CLEAR" ("UNARCHIVED")? "LOGFILE"
	  {logfile_descriptor, ","} ("UNRECOVERABLE" "DATAFILE")?
	;

logfile_descriptor :
	  logfile_group_spec
	| "(" {filename, ","} ")"
	| filename
	;

controlfile_clauses :
	  "CREATE" "STANDBY" "CONTROLFILE" "AS" filename (reuse_tag)?
	| "BACKUP" "CONTROLFILE" "TO"
	  (filename (reuse_tag)? | trace_file_clause)
	;

trace_file_clause :
	"TRACE" (resetlogs_spec)? ;

standby_database_clauses :
	( "ACTIVATE" ("PHYSICAL" | "LOGICAL")?
	  "STANDBY" "DATABASE" ("SKIP" ("STANDBY" "LOGFILE")?)?
	| "SET" "STANDBY" "DATABASE" "TO" "MAXIMIZE"
	    ("PROTECTION" | "AVAILABILITY" | "PERFORMANCE")
	| "REGISTER" ("OR" "REPLACE")? ("PHYSICAL" | "LOGICAL")? "LOGFILE"
	                    ({redo_log_file_spec, ","})?
	| commit_switchover_clause
	| "START" "LOGICAL" "STANDBY" "APPLY"
	     ("NEW" "PRIMARY" dblink | "INITIAL" (scn_value)?)
	| ("STOP" | "ABORT") "LOGICAL" "STANDBY" "APPLY"
	)
	(parallel_clause)? ;

	scn_value 	  : expression;
	character_set : plsql_identifier;

commit_switchover_clause :
	"COMMIT" "TO" "SWITCHOVER" "TO"
	("PHYSICAL" | "LOGICAL") ("PRIMARY" | "STANDBY")
	(("WITH" | "WITHOUT") "SESSION" "SHUTDOWN")?

	("WAIT" | "NOWAIT")? ;

default_settings_clauses :
	  character_set_clause
	| set_time_zone_clause
	| "DEFAULT" "TEMPORARY" tablespace_spec
	| "RENAME" "GLOBAL_NAME" "TO" database "." {domain,"."}
	;

set_time_zone_clause :
	"SET" "TIME_ZONE" "=" string ;
	// ' { { + | - } hh : mi | time_zone_region } '

conversion_clauses :
	  "RESET" "COMPATIBILITY"
	| "CONVERT"
	;

redo_thread_clauses :
	  "ENABLE" ("PUBLIC")? "THREAD" integer
	| "DISABLE" "THREAD" integer
	;

security_clause :
	"GUARD" ("ALL" | "STANDBY" | "NONE");

alter_cluster_statement :
	"ALTER" "CLUSTER" (schema ".")? cluster
	{ physical_attributes_clause
	| size_spec
	| allocate_extent_clause
	| deallocate_unused_clause
	| cache_clause
	}
	(parallel_clause)?
	;

allocate_extent_clause :
	"ALLOCATE" "EXTENT"
	("(" {size_spec
	   | "DATAFILE" filename
	   | "INSTANCE" integer }
	 ")"
	)?;

deallocate_unused_clause :
	"DEALLOCATE" "UNUSED"
	("KEEP" integer ( K_or_M )?)? ;

alter_dimension_statement :
	"ALTER" "DIMENSION" qualified_name
	{ "ADD"
	      ( level_clause
	      | hierarchy_clause
	      | attribute_clause
	      )
	| "DROP"
	      ("LEVEL" level ("RESTRICT" | cascade_tag)?
	      | "HIERARCHY" hierarchy
	      | "ATTRIBUTE" level
	      )
	| compile_clause
	};

level_clause :
	"LEVEL" level "IS"
	( table "." column
	| "(" {table "." column, ","} ")"
	);

hierarchy_clause :
	"HIERARCHY" hierarchy
	"(" child_level {"CHILD" "OF" parent_level}
	    (join_clause)? ")";

join_clause :
	{"JOIN" "KEY"
	(child_key_column | "(" {child_key_column, ","} ")")
		"REFERENCES" parent_level } ;

attribute_clause :
	"ATTRIBUTE" level "DETERMINES"
	(dependent_column | "(" {dependent_column, ","} ")") ;

level : identifier;
child_level : level;
parent_level : level;
hierarchy : identifier;
child_key_column : column;
dependent_column : column;

alter_function_statement :
	"ALTER" "FUNCTION" (schema ".")? function_name
	compile_clause (reuse_settings)?;

alter_procedure_statement :
	"ALTER" "PROCEDURE" (schema ".")? procedure_name
	compile_clause (reuse_settings)?;

alter_profile_statement :
	"ALTER" "PROFILE" profile_name "LIMIT"
	{resource_parameter | password_parameter} ;

resource_parameter :
	  resource_parameter_name
	  resource_parameter_value ;

resource_parameter_name :
	  "SESSIONS_PER_USER"
	| "CPU_PER_SESSION"
	| "CPU_PER_CALL"
	| "CONNECT_TIME"
	| "IDLE_TIME"
	| "LOGICAL_READS_PER_SESSION"
	| "LOGICAL_READS_PER_CALL"
	| "COMPOSITE_LIMIT"
	| "PRIVATE_SGA"
	;

resource_parameter_value :
	  integer (K_or_M)?
	| "UNLIMITED"
	| default_tag ;

password_parameter :
	password_parameter_name
	password_parameter_value ;

password_parameter_name :
	  "FAILED_LOGIN_ATTEMPTS"
	| "PASSWORD_LIFE_TIME"
	| "PASSWORD_REUSE_TIME"
	| "PASSWORD_REUSE_MAX"
	| "PASSWORD_LOCK_TIME"
	| "PASSWORD_GRACE_TIME"
	| "PASSWORD_VERIFY_FUNCTION"
	;

password_parameter_value :
	  "UNLIMITED"
	| default_tag
	| expression
	| function
	| null_tag
	;

alter_index_statement :
	"ALTER" "INDEX" (schema ".")? index
	({ deallocate_unused_clause
	| allocate_extent_clause
	| parallel_clause
	| physical_attributes_clause
	| logging_clause
	| rebuild_clause
	| "PARAMETERS" "(" alter_index_params ")"
	| enable_or_disable
	| unusable_tag
	| rename_partition_clause
	| coalesce_tag
	| monitoring_spec "USAGE"
	| update_block_references_tag
	| alter_index_partitioning
	})?
	;

rename_partition_clause :
	"RENAME" ("PARTITION" partition)? "TO" new_name ;

	alter_index_params : string;
	new_name : identifier;

rebuild_clause :
	"REBUILD"
	(   "PARTITION" partition
	  | "SUBPARTITION" subpartition
	  | rebuild_reverse_clause
	)?
	({  parallel_clause
	  | tablespace_spec
	  | "PARAMETERS" "(" rebuild_paramaters ")"
	  | online_tag
	  | compute_statistics_tag
	  | physical_attributes_clause
	  | key_compression
	  | logging_clause
	})? ;

	rebuild_paramaters : string;

	rebuild_reverse_clause :
	    "REVERSE"
	  | "NOREVERSE" ;

alter_index_partitioning :
	  modify_index_default_attrs
	| modify_index_partition
	| rename_index_partition
	| drop_index_partition
	| split_index_partition
	| modify_index_subpartition
	;

modify_index_default_attrs :
	"MODIFY" "DEFAULT" "ATTRIBUTES" ("FOR" "PARTITION" partition)?
	{ physical_attributes_clause
	| "TABLESPACE" (tablespace | default_tag)
	| logging_clause
	} ;

modify_index_partition :
	"MODIFY" "PARTITION" partition
	( { physical_attributes_clause
	  | logging_clause
	  | deallocate_unused_clause
	  | allocate_extent_clause
	  }
	| "PARAMETERS" "(" alter_partition_params ")"
	| coalesce_tag
	| update_block_references_tag
	| unusable_tag
	);

	alter_partition_params : string;
	partition_name : identifier;
	partition_name_old : identifier;

rename_index_partition :
	"RENAME"
	(  "PARTITION" partition
	 | "SUBPARTITION" subpartition)
	"TO" new_name ;

drop_index_partition :
	"DROP" "PARTITION" partition_name ;


split_index_partition :
	"SPLIT" "PARTITION" partition_name_old
	"AT" "(" {value, ","} ")"
	("INTO" "(" {index_partition_description, ","} ")")?
	(parallel_clause)?;

index_partition_description :
	"PARTITION"
	(partition ({ segment_attributes_clause
			| key_compression })?)? ;

modify_index_subpartition :
	"MODIFY" "SUBPARTITION" subpartition
	( unusable_tag
	| allocate_extent_clause
	| deallocate_unused_clause
	);

alter_indextype_statement :
	"ALTER" "INDEXTYPE" qualified_name
	{ {("ADD" | "DROP") (schema ".")? operator_name
	"(" parameter_types ")", ","}
	  (using_type_clause)?
	| compile_clause
	} ;


	operator_name : plsql_identifier;
	parameter_types : {identifier, ","};

using_type_clause :
	"USING" qualified_name;

alter_operator_statement :
	"ALTER" "OPERATOR"
	(schema ".")? operator_name "COMPILE";

alter_outline_statement :
	"ALTER" "OUTLINE" ("PUBLIC" | "PRIVATE")? outline
	  { rebuild_tag
	  | "RENAME" "TO" new_outline_name
	  | "CHANGE" "CATEGORY" "TO"  new_category_name
	  } ;

	outline : plsql_identifier;
	new_outline_name : plsql_identifier;
	new_category_name : plsql_identifier;

alter_package_statement :
	"ALTER" "PACKAGE" (schema ".")? package
	compile_clause
	("PACKAGE" | "SPECIFICATION" | "BODY")?
	(reuse_settings)? ;

alter_resource_statement :
	"ALTER" "RESOURCE" "COST"
	{alter_resource_clause} ;

alter_resource_clause :
	resource_name resource_value ;

resource_name :
	  "CPU_PER_SESSION"
	| "CONNECT_TIME"
	| "LOGICAL_READS_PER_SESSION"
	| "PRIVATE_SGA" ;

resource_value :
	integer ;

alter_role_statement :
	"ALTER" "ROLE" role
	role_identity_clause ;

role_identity_clause :
	  not_identified_spec
	| "IDENTIFIED"
		("BY" password
		 | using_package_clause
		 | externally_or_globally) ;

externally_or_globally :
	  "EXTERNALLY"
	| "GLOBALLY" ("AS" external_name)? ;

not_identified_spec :
	"NOT" "IDENTIFIED" ;

using_package_clause :
	"USING" qualified_name ;

alter_rollback_statement :
	"ALTER" "ROLLBACK" "SEGMENT" rollback_segment
	( online_or_offline
	| storage_clause
	| shrink_clause
	)
	;

shrink_clause :
	"SHRINK" ("TO" integer ( K_or_M )? )?	;

alter_sequence_statement :
	"ALTER" "SEQUENCE" (schema ".")? sequence
	{ increment_by_clause
	| maxvalue_clause
	| minvalue_clause
	| cycle_or_nocycle
	| cache_clause
	| order_or_noorder
	}
	;

increment_by_clause :
	"INCREMENT" "BY" integer ;

maxvalue_clause :
	  "MAXVALUE" integer
	| "NOMAXVALUE" ;

minvalue_clause :
	  "MINVALUE" integer

	| "NOMINVALUE" ;

cycle_or_nocycle :
	  "CYCLE"
	| "NOCYCLE" ;

order_or_noorder :
	  "ORDER"
	| "NOORDER" ;

alter_session_statement :
	"ALTER" "SESSION"
	("ADVISE" ("COMMIT" | "ROLLBACK" | "NOTHING")
	| "CLOSE" "DATABASE" "LINK" dblink
	| enable_or_disable "COMMIT" "IN" "PROCEDURE"
	| ("ENABLE" | "DISABLE" | "FORCE")
	   "PARALLEL" ("DML" | "DDL" | "QUERY")
	   ("PARALLEL" integer)?
	| ("ENABLE" "RESUMABLE" ("TIMEOUT" integer)? ("NAME" string)?
	   | "DISABLE" "RESUMABLE")
	| alter_session_set_clause
	)
	;

alter_session_set_clause :
	"SET" {parameter_name ("=")? parameter_value}
	("COMMENT" "=" string)? ;

alter_snapshot_statement :
	"ALTER" "SNAPSHOT"
	("LOG" "ON")?
	snapshot_name
	(refresh_clause)?
	("ADD" "PRIMARY" "KEY")?
	(storage_clause)? ;

alter_system_statement :
	"ALTER" "SYSTEM"
	( archive_log_clause
	| "CHECKPOINT" ( "GLOBAL" | "LOCAL" )?
	| "CHECK" "DATAFILES" ( "GLOBAL" | "LOCAL" )?
	| enable_or_disable "DISTRIBUTED" "RECOVERY"
	| enable_or_disable "RESTRICTED" "SESSION"
	| "FLUSH" "SHARED_POOL"
	| end_session_clauses
	| "SWITCH" "LOGFILE"
	| ("SUSPEND" | "RESUME")
	| ("QUIESCE" "RESTRICTED" | "UNQUIESCE")
	| "SHUTDOWN" ("IMMEDIATE")? dispatcher_name
	| "REGISTER"
	| "SET" {alter_system_set_clause, (",")?}
	| "RESET" {alter_system_set_clause, (",")?}
	)
	;

end_session_clauses :
	("DISCONNECT" "SESSION" string  ("POST_TRANSACTION")?
	| "KILL" "SESSION" string
	)
	("IMMEDIATE")?;

archive_log_clause :
	"ARCHIVE" "LOG" ("THREAD" integer)?
	( ( "SEQUENCE" integer
	  | "CHANGE" integer
	  | "CURRENT" ("NOSWITCH")?
	  | logfile_group_spec
	  | "LOGFILE" filename ("USING" "BACKUP" "CONTROLFILE")?
	  | "NEXT"
	  | "ALL"
	  | "START"

	  )
	  ("TO" location)?
	| "STOP"
	);

	dispatcher_name : identifier;

alter_system_set_clause :
	parameter_name ("=")? {parameter_value, ","}
	("COMMENT" string)? ("DEFERRED")?
	("SCOPE" "=" ("MEMORY" | "SPFILE" | "BOTH"))?
	("SID" "=" (string | "*"))?;


alter_table_statement :
	"ALTER" "TABLE" (schema ".")? table
	( alter_table_properties
	| column_clauses
	| constraint_clauses
	| alter_table_partitioning

	| alter_external_table_clauses
	| move_table_clause
	)?
	({ enable_disable_clause
	 | enable_or_disable ("TABLE" "LOCK" | "ALL" "TRIGGERS")
	 })?
	;

alter_table_properties :
	((
	  { physical_attributes_clause
	  | logging_clause
	  | data_segment_compression
	  | supplemental_lg_grp_clauses
	  | allocate_extent_clause
	  | deallocate_unused_clause
	  | cache_clause
	  | monitoring_spec
	  | upgrade_table_clause
	  | records_per_block_clause
	  | parallel_clause
	  | row_movement_clause
	  }
	| "RENAME" "TO" new_table_name
	)?
	(alter_iot_clauses)?) <1,> ;

column_clauses :
	{   add_column_clause
	  | modify_column_clause
	  | drop_column_clause
	}
	| rename_column_clause
	| {modify_collection_retrieval}
	| modify_LOB_storage_clause
	| alter_varray_col_properties
	;

add_column_clause :
	"ADD"
	(
		"(" {  column datatype (default_tag expression)?
		      ({inline_constraint})?
		      (inline_ref_constraint)?
			, "," }
		")"
	|
		column datatype (default_tag expression)?
	      ({inline_constraint})?
	      (inline_ref_constraint)?
	)
	(column_properties)? ;

modify_column_clause :
	"MODIFY"
	( modify_col_properties
	| substitutable_clause );

modify_col_properties :
	"("
		{column (datatype)? (default_tag expression)?
			({inline_constraint })?, ","}
	")"
	( modify_LOB_storage_clause )? ;

substitutable_clause :
	(not_tag)? "SUBSTITUTABLE"
	"AT" "ALL" "LEVELS" (force_tag)? ;

drop_column_clause :
	 "SET" "UNUSED" ("COLUMN" column | column_list)
	  ({cascade_constraints_tag | "INVALIDATE"})?
	| "DROP" ("COLUMN" column | column_list)
	  ({cascade_constraints_tag | "INVALIDATE"})?
	  ("CHECKPOINT" integer)?
	| "DROP" ("UNUSED" "COLUMNS" | "COLUMNS" "CONTINUE")
	  ("CHECKPOINT" integer)?
	;

rename_column_clause :
	"RENAME" "COLUMN" old_name "TO" new_name ;

modify_collection_retrieval :
	"MODIFY" "NESTED" "TABLE" collection_item
	"RETURN" "AS" ("LOCATOR" | "VALUE");

constraint_clauses :
	 "ADD"
	 ( {out_of_line_constraint}
	   | out_of_line_REF_constraint
	   | "(" {out_of_line_constraint} ")"
	   | "(" out_of_line_REF_constraint ")"
	 )
	| "MODIFY" "CONSTRAINT" constraint constraint_state
	| "RENAME" "CONSTRAINT" old_name "TO" new_name
	| drop_constraint_clause
	;

drop_constraint_clause :
	"DROP"
	( (primary_key_spec | unique_columns_spec)
	  (cascade_tag)? (keep_or_drop_index)?
	| "CONSTRAINT" constraint (cascade_tag)?
	);

alter_table_partitioning :
	  modify_table_default_attrs
	| set_subpartition_template
	| modify_table_partition
	| modify_table_subpartition
	| move_table_partition
	| move_table_subpartition
	| add_table_partition
	| coalesce_table_partition
	| drop_table_partition
	| drop_table_subpartition
	| rename_partition_subpart
	| truncate_partition_subpart
	| split_table_partition
	| split_table_subpartition
	| merge_table_partitions
	| merge_table_subpartitions
	| exchange_partition_subpart
	;

modify_table_default_attrs :
	"MODIFY" "DEFAULT" "ATTRIBUTES"
	("FOR" "PARTITION" partition)?
	(segment_attributes_clause)?
	(data_segment_compression)?
	(pctthreshold_clause)?
	(key_compression)?
	(alter_overflow_clause)?
	({("LOB" "(" LOB_item ")" | "VARRAY" varray)
		"(" LOB_parameters ")"})?;

alter_overflow_clause :
	"OVERFLOW"
	{  allocate_extent_clause
	 | deallocate_unused_clause }
	| add_overflow_clause ;

add_overflow_clause :
	"ADD" "OVERFLOW" (segment_attributes_clause)?
	("(" {"PARTITION" (segment_attributes_clause)? , ","} ")")? ;

set_subpartition_template :
	"SET" "SUBPARTITION" "TEMPLATE"
	(
		"(" { "SUBPARTITION" subpartition
			(list_values_clause)?
			(partitioning_storage_clause)?
			, ","
		    }
		")"
	| hash_subpartition_quantity
	);

modify_table_partition :
	  modify_range_partition
	| modify_hash_partition
	| modify_list_partition
	;

modify_range_partition :

	"MODIFY" "PARTITION" partition
	  ( partition_attributes
	  | add_hash_subpartition
	  | add_list_subpartition
	  | "COALESCE" "SUBPARTITION"
	    (update_global_index_clause)?
	    (parallel_clause)?
	  | alter_mapping_table_clause
	  | unusable_local_indexes_clause
	  );

partition_attributes :
	(({ physical_attributes_clause
	 | logging_clause
	 | allocate_extent_clause
	 | deallocate_unused_clause
	 })?
	("OVERFLOW"
	 { physical_attributes_clause
	 | logging_clause
	 | allocate_extent_clause
	 | deallocate_unused_clause
	 })?
	( data_segment_compression )?
	({ ("LOB" LOB_item | "VARRAY" varray)
		modify_LOB_parameters })?) <1,> ;

add_hash_subpartition :
	"ADD" subpartition_spec
	(update_global_index_clause)?
	(parallel_clause)?;

add_list_subpartition :
	"ADD" subpartition_spec ;


update_global_index_clause :
	("UPDATE" | "INVALIDATE") "GLOBAL" "INDEXES" ;

alter_mapping_table_clause :
	"MAPPING" "TABLE"
	( update_block_references_tag
	| allocate_extent_clause
	| deallocate_unused_clause
	);

modify_hash_partition :
	"MODIFY" "PARTITION" partition
	  ( partition_attributes
	  | alter_mapping_table_clause
	  | unusable_local_indexes_clause
	  );

modify_list_partition :
	"MODIFY" "PARTITION" partition
	  ( partition_attributes
	  | ("ADD" | "DROP") "VALUES" "(" {partition_value, ","} ")"
	  | unusable_local_indexes_clause
	  );

modify_table_subpartition :
	"MODIFY" "SUBPARTITION" subpartition
	( modify_hash_subpartition
	| modify_list_subpartition );

modify_hash_subpartition :
	( ( (allocate_extent_clause | deallocate_unused_clause)
	  | {("LOB" LOB_item | "VARRAY" varray) modify_LOB_parameters}
	  )
	| unusable_local_indexes_clause
	);

modify_list_subpartition :
	(allocate_extent_clause | deallocate_unused_clause)
	| {("LOB" LOB_item | "VARRAY" varray) modify_LOB_parameters}
	| unusable_local_indexes_clause
	| ("ADD" | "DROP") "VALUES" {value,","}
	;

move_table_partition :
	"MOVE" "PARTITION" partition
	("MAPPING" "TABLE")?
	(table_partition_description)?
	(update_global_index_clause)?
	(parallel_clause)? ;

move_table_subpartition :
	"MOVE" "SUBPARTITION" subpartition
//	subpartition_attributes
	(update_global_index_clause)?
	(parallel_clause)?;

partition_spec :
	"PARTITION" partition
	(tablespace_spec)?
	({ LOB_storage_clause
	 | "(" minextents_spec ")"})? ;

add_table_partition :
	  add_range_partition_clause
	| add_hash_partition_clause
	| add_list_partition_clause
	;

add_range_partition_clause :
	"ADD" "PARTITION" (partition)?
	range_values_clause
	(table_partition_description)?;

add_hash_partition_clause :
	"ADD" "PARTITION" (partition)?
	partitioning_storage_clause
	(update_global_index_clause)?
	(parallel_clause)? ;

add_list_partition_clause :
	"ADD" "PARTITION" (partition)?
	list_values_clause
	(table_partition_description)? ;

coalesce_table_partition :
	"COALESCE" "PARTITION"
	(update_global_index_clause)?
	(parallel_clause)? ;

drop_table_partition :
	"DROP" "PARTITION" partition
	(update_global_index_clause
	(parallel_clause)?)? ;

drop_table_subpartition :
	"DROP" "SUBPARTITION" subpartition
	(update_global_index_clause
	(parallel_clause)?)? ;

rename_partition_subpart :
	"RENAME" ("PARTITION" | "SUBPARTITION")
	current_name "TO" new_name ;

	current_name : identifier;
	current_partition : identifier;
	partition_1 : identifier;
	partition_2 : identifier;
	subpart_1 : identifier;
	subpart_2 : identifier;

truncate_partition_subpart :
	"TRUNCATE" ("PARTITION" partition
		| "SUBPARTITION" subpartition)
	(reuse_or_drop_storage)?
	(update_global_index_clause (parallel_clause)?)? ;

split_table_partition :
	"SPLIT" "PARTITION" current_partition
	("AT" | "VALUES") "(" {value,","} ")"
	("INTO" "(" partition_spec "," partition_spec ")")?
	(update_global_index_clause)?
	(parallel_clause)? ;

split_table_subpartition :
	"PARTITION" (partition)?
	(table_partition_description)? ;

merge_table_partitions :
	"MERGE" "PARTITIONS" partition_1 "," partition_2
	("INTO" partition_spec)?
	(update_global_index_clause)?
	(parallel_clause)? ;

merge_table_subpartitions :
	"MERGE" "SUBPARTITIONS" subpart_1 "," subpart_2
	("INTO" subpartition_spec)?
	(update_global_index_clause)?
	(parallel_clause)? ;

exchange_partition_subpart :
	"EXCHANGE" ("PARTITION" partition | "SUBPARTITION" subpartition)
	"WITH" "TABLE" table (("INCLUDING" | "EXCLUDING") "INDEXES")?
	(("WITH" | "WITHOUT") "VALIDATION")?
	(exceptions_clause)?
	(update_global_index_clause
		(parallel_clause)?)?;

exceptions_clause :
	"EXCEPTIONS" "INTO" (schema ".")? table ;

alter_external_table_clauses :
	{ add_column_clause
	| modify_column_clause
	| drop_column_clause
	| parallel_clause
	| external_data_properties
	| "REJECT" "LIMIT" (integer | "UNLIMITED")
	};

move_table_clause :
	"MOVE" (online_tag)?
	(segment_attributes_clause)?
	(data_segment_compression)?
	(index_org_table_clause)?
	({LOB_storage_clause | varray_col_properties})?
	(parallel_clause)? ;

supplemental_lg_grp_clauses :
	  "ADD" "SUPPLEMENTAL" "LOG" "GROUP" log_group
	  column_list ("ALWAYS")?
	| "DROP" "SUPPLEMENTAL" "LOG" "GROUP" log_group
	;

upgrade_table_clause :
	"UPGRADE" ((not_tag)? "INCLUDING" "DATA")?
	(column_properties)?;

records_per_block_clause :
	("MINIMIZE" | "NOMINIMIZE") "RECORDS_PER_BLOCK" ;

new_table_name : identifier;

alter_iot_clauses :
	  index_org_table_clause
	| alter_overflow_clause
	| alter_mapping_table_clauses
	| coalesce_tag ;

modify_LOB_storage_clause :
	"MODIFY" "LOB" "(" LOB_item ")"
	"(" modify_LOB_parameters ")" ;

modify_LOB_parameters :
	{ storage_clause
	| pctversion_clause
	| retention_tag
	| freepools_clause
	| "REBUILD" "FREEPOOLS"
	| cache_clause
	| ( allocate_extent_clause | deallocate_unused_clause )
	};

alter_varray_col_properties :
	"MODIFY" "VARRAY" varray_item
	"(" modify_LOB_storage_parameters ")" ;

alter_tablespace_statement :
	"ALTER" tablespace_spec
	{ datafile_tempfile_clauses
	| default_storage_clause
	| minimum_extent_clause
	| online_tag
	| offline_clause
	| ("BEGIN" | "END") "BACKUP"
	| read_write_clause
	| permanent_or_temporary
	| coalesce_tag
	| logging_clause
	| force_logging_spec
	| autoextend_clause
	| size_spec
	}
	;

read_write_clause :
	  "READ" "ONLY"
	| "READ" "WRITE" ;

offline_clause :
	"OFFLINE" (offline_attribute)? ;

offline_attribute :
	"NORMAL" | "TEMPORARY" | "IMMEDIATE" ;

default_storage_clause :
	"DEFAULT" (data_segment_compression)?
	storage_clause ;

datafile_tempfile_clauses :
	( "ADD" ("DATAFILE" | "TEMPFILE")
	      ({datafile_tempfile_spec, ","})?
	| "RENAME" "DATAFILE" {filename, ","}
		"TO" {filename, ","}
	| ("DATAFILE" | "TEMPFILE") online_or_offline
	) ;

alter_trigger_statement :
	"ALTER" "TRIGGER" ( schema "." )?trigger_name
	  { enable_or_disable
	  | "RENAME" "TO" new_name
	  | compile_clause (reuse_settings)?
	  } ;

alter_type_statement :
	"ALTER" "TYPE" ( schema "." )? type
	  ( compile_type_clause
	  | replace_type_clause
	  | ( alter_method_spec
	    | alter_attribute_definition
	    | (not_tag)? (instantiable_tag | final_tag)
	    )
	    ( dependent_handling_clause )?
	  ) ;

compile_type_clause :
	compile_clause
	("SPECIFICATION "| "BODY")?
	(reuse_settings)? ;

replace_type_clause :
	"REPLACE" (invoker_rights_clause)? "AS" "OBJECT"
	"(" {attribute datatype, ","}
	  ("," {element_spec, ","} )? ")" ;

alter_method_spec :
	("ADD" | "DROP")
	{map_order_function_spec | subprogram_clauses, ","} ;


alter_attribute_definition :
	  ("ADD" | "MODIFY") "ATTRIBUTE"
	  (attribute (datatype)? | "(" {attribute datatype, ","} ")" )
	| "DROP" "ATTRIBUTE" (attribute | "(" {attribute, ","} ")" ) ;

dependent_handling_clause :
	  "INVALIDATE"
	| "CASCADE" ( ((not_tag)? "INCLUDING" "TABLE" "DATA"
	            | "CONVERT" "TO" "SUBSTITUTABLE"
	            ))? ((force_tag)? exceptions_clause)?
	;

alter_user_statement :
	"ALTER" "USER" {user_name, ","}
	({create_user_option})? ;

proxy_clause :
	("GRANT" | "REVOKE") "CONNECT" "THROUGH" proxy
	("WITH"
	  (ROLE ( {role, ","} | "ALL" "EXCEPT" {role, ","} )
	 | "NO" "ROLES" )
	)?
	("AUTHENTICATED" "USING"
	 ( "PASSWORD"
	 | "DISTINGUISHED" "NAME"
	 | "CERTIFICATE" ("TYPE" string)? ("VERSION" string)?
	 )
	)?;

alter_view_statement :
	"ALTER" "VIEW" ( schema "." )? view
	(  "ADD" out_of_line_constraint
	 | "MODIFY" "CONSTRAINT" constraint ("RELY" | "NORELY")
	 | "DROP"
	   ( "CONSTRAINT" constraint
	   | primary_key_spec
	   | unique_columns_spec )
	 | compile_clause
	) ;

alter_materialized_view_statement :
	"ALTER" "MATERIALIZED" "VIEW"
	(schema ".")? materialized_view
	( physical_attributes_clause
	  | data_segment_compression
	  | {LOB_storage_clause, ","}
	  | {modify_LOB_storage_clause, ","}
	  | alter_table_partitioning
	  | parallel_clause
	  | logging_clause
	  | allocate_extent_clause
	  | cache_or_nocache
	)?
	(alter_iot_clauses)?
	("USING" "INDEX" physical_attributes_clause)?
	(   "MODIFY" scoped_table_ref_constraint
	  | rebuild_tag
	  | mv_refresh
	)?
	( ("ENABLE" | "DISABLE") "QUERY" "REWRITE"
	  | "COMPILE"
	  | "CONSIDER" "FRESH"
	)? ;

alter_materialized_view_log :
	"ALTER" "MATERIALIZED" "VIEW" "LOG" "ON"
	(schema ".")? table
	(   physical_attributes_clause
	  | alter_table_partitioning
	  | parallel_clause
	  | logging_clause
	  | allocate_extent_clause
	  | cache_or_nocache
	)?
	("ADD"
	      { ("OBJECT" "ID" | primary_key_spec | "ROWID")
	        (column_list)?
	      | column_list
	      , "," }
	      ( new_values_clause )?
	)?;

analyze_statement :
	"ANALYZE"
	(   "TABLE" ( schema "." )? table
	      ( "PARTITION" "(" partition ")"
	      | "SUBPARTITION" "(" subpartition ")" )?
	  | "INDEX" ( schema "." )?index
	      ( "PARTITION" "(" partition ")"
	      | "SUBPARTITION" "(" subpartition ")" )?
	  | "CLUSTER" ( schema "." )? cluster
	)
	{   compute_statistics_clause
	  | estimate_statistics_clause
	  | validation_clauses
	  | list_chained_rows_clause
	  | delete_statistics_clause
	} ;

compute_statistics_clause :
	"COMPUTE" ( "SYSTEM" )?
	"STATISTICS" (for_clause)? ;

estimate_statistics_clause :
	"ESTIMATE" ( "SYSTEM" )?
	"STATISTICS" (for_clause)?
	("SAMPLE" integer ("ROWS" | "PERCENT"))? ;

delete_statistics_clause :
	"DELETE" ( "SYSTEM" )? "STATISTICS" ;

list_chained_rows_clause :
	"LIST" "CHAINED" "ROWS" (into_clause)? ;

validation_clauses :
	  "VALIDATE" "REF" "UPDATE"
	  ("SET" "DANGLING" "TO" "NULL")?
	| "VALIDATE" "STRUCTURE"
	  (cascade_tag)? (into_clause)?
	  (online_or_offline)?
	;

for_clause :
	{"FOR"
	( "TABLE"
	| "ALL" ("INDEXED")? "COLUMNS" (size_spec)?
	| "COLUMNS" (size_spec)?
	  {(column | attribute) (size_spec)?}
	| "ALL" ("LOCAL")? "INDEXES"
	)} ;

into_clause :
	"INTO" (schema ".")? table ;

audit_statement :
	"AUDIT" (sql_statement_clause | schema_object_clause)
	("BY" ("SESSION" | "ACCESS"))?
	("WHENEVER" (not_tag)? "SUCCESSFUL")?;

sql_statement_clause :
	( {statement_option | "ALL", ","}
	| {system_privilege | "ALL" "PRIVILEGES", ","}
	)
	(auditing_by_clause | auditing_on_clause)? ;

auditing_by_clause :
	"BY"
	( {proxy, ","} ("ON" "BEHALF" "OF" {user_name, ","} | "ANY")?
	| {user_name, ","}
	) ;

schema_object_clause :
	({object_option, ","} | "ALL")
	auditing_on_clause ;

auditing_on_clause :
	  "ON"
	  (  (schema ".")? object_name
	   | "DIRECTORY" directory
	   | default_tag ) ;

statement_option :
	(sql_verb)? ("ANY" | "PUBLIC")? sql_item ;

system_privilege  :
	  sql_verb ("ANY" | "PUBLIC")? sql_item
	| ("GLOBAL")? "QUERY" "REWRITE"
	| "ON" "COMMIT" "REFRESH"
	| "RESTRICTED" "SESSION"
	| "UNLIMITED" "TABLESPACE"
	| "ANALYZE" "ANY"
	| "AUDIT" "ANY"
	| "COMMENT" "ANY" "TABLE"
	| "EXEMPT" "ACCESS" "POLICY"
	| "FORCE" ("ANY")? "TRANSACTION"
	| "RESUMABLE"
	| "SYSDBA"
	| "SYSOPER"
	;

sql_verb :
	  "BACKUP" | "CREATE" | "ALTER" | "DROP" | "AUDIT" | "DEBUG" | "EXECUTE"
	| "GRANT" | "SELECT" | "DELETE" | "INSERT" | "LOCK" | "FLASHBACK"
	| "UPDATE" | "MANAGE" | "ADMINISTER" | "BECOME" | "UNDER" | "READ" ;

sql_item :
	  "CLUSTER" | "CONTEXT" | "DATABASE" "LINK" | "SYSTEM"
	| "CONNECT" "SESSION" | "DIMENSION" | "DIRECTORY"
	| "INDEXTYPE" | "INDEX" | "LIBRARY" | "MATERIALIZED" "VIEW"
	| "OPERATOR" | "OUTLINE" | "PROCEDURE" | "PROFILE" | "ROLE"
	| "ROLLBACK" "SEGMENT" | "SEQUENCE" | "SESSION"
	| "RESOURCE" "COST" | "SYNONYM" | "TABLE" | "TABLESPACE"
	| ("DATABASE")? "TRIGGER" | "TYPE" | "USER" | "VIEW"
	| ("OBJECT")? "PRIVILEGE" | "DICTIONARY"
	;

object_option  :
	"ALTER" | "AUDIT" | "COMMENT" | "DELETE" | "EXECUTE"
	| "GRANT" | "INDEX" | "INSERT" | "LOCK" | "READ" | "RENAME"
	| "SELECT" | "UPDATE"
	;

comment_statement :
	"COMMENT" "ON"
	( "TABLE" qualified_name
	| "COLUMN" qualified_name
	| "OPERATOR" qualified_name
	| "INDEXTYPE" qualified_name
	)
	"IS" string;

expression :
	(
	  not_tag expression
	| exists_condition
	| under_path_condition
 	| "(" expression ")"
 	| "(" expression_list ")"
 	| interval_expression
	| ({"+" | "-" | "PRIOR"})?
 	(
		  simple_expression
		| case_expression
		| cursor_expression
		| object_access_expression
		| scalar_subquery_expression
		| type_constructor_expression
		| variable_expression
	)
	)
	(
	   interval_specifier
	 | datetime_specifier
	 | null_condition
	 | like_condition
	 | membership_condition
	 | range_condition
	 | is_of_type_condition
	 | equals_path
	)?
	(operator expression)?
	;

datetime_specifier :
	"AT"
	( "LOCAL"
	  | "TIME" "ZONE"
	  	(   string
	  	  | "DBTIMEZONE"
        	  | "SESSIONTIMEZONE"
	        | expression
            )
	) ;

interval_expression :
	"INTERVAL" squotedstring "DAY" ;

interval_specifier :
	  "DAY" "TO" "SECOND"
	| "YEAR" "TO" "MONTH" ;

exists_condition :
	"EXISTS" "(" subquery ")" ;

under_path_condition :
	"UNDER_PATH"
	"(" column ("," levels)? ","
		path_string ("," correlation_integer)? ")" ;

null_condition :
	"IS" (not_tag)? null_tag ;

like_condition :
	(not_tag)? ("LIKE" | "LIKEC" | "LIKE2" | "LIKE4")
	expression ("ESCAPE" expression)? ;

membership_condition :
	(not_tag)? "IN" "(" (expression_list | subquery) ")" ;

range_condition :
	(not_tag)? "BETWEEN"
	simple_expression "AND" simple_expression ;

is_of_type_condition :
	"IS" (not_tag)? "OF" ("TYPE")?
	"(" {("ONLY")? (schema ".")? type ")", ","} ;

equals_path :
	"EQUALS_PATH"
	"(" column "," path_string ("," correlation_integer)? ")" ;

operator :
	  "*"  | "/" | "+" | "-"  | "AND" | "OR"
	| "<"  | ">" | "=" | "<=" | ">="  | "!="
	| "^=" | "<>" | "||" | "**"
	| "=>"
	| ":="
	;

simple_expression :
	  "ROWNUM"
	| null_tag
	| "USER"
	| function ("." simple_expression)?
	| ("DISTINCT")?
	  qualified_name (  "(" ({expression, ","})? ")"
				| "(" "+" ")"
				| "." ("CURRVAL" | "NEXTVAL")
				| "%" ("FOUND" | "NOTFOUND")
				)?
				("." simple_expression
				| "@" simple_expression)?
	| number
	| hex_number
	| squotedstring
	| quotedstring
	;

case_expression :
	"CASE"
	( simple_case_expression
	| searched_case_expression )
	(else_clause)? "END" ;

simple_case_expression :
	expression
	{"WHEN" comparison_expr "THEN" return_expr} ;

searched_case_expression :
	{"WHEN" condition "THEN" return_expr} ;

else_clause :
	"ELSE" else_expr ;

cursor_expression :
	"CURSOR" "(" subquery ")" ;

function :
	  single_row_function
	| aggregate_function
	| object_reference_function
	| value_function
	| analytic_function
	| xml_function
	;

single_row_function :
	  character_function
	| conversion_function
	| treat_function
	| miscellaneous_single_row_function
	;

aggregate_function :
	(
	aggregate_function_name
	"(" ("*" | { (distinct_or_all)? expression, ","})? ")"
	( "OVER" "("
		( analytic_clause
		| (query_partition_clause)? order_by_clause) ")"
	| "WITHIN" "GROUP"
		"(" "ORDER" "BY"
		  {expression (ascending_or_descending)? ("NULLS" ("FIRST" | "LAST"))?, ","}
		 ")"
		("OVER" "(" query_partition_clause ")")?
	)?
	)
	(
		"KEEP"
		"(" "DENSE_RANK" ("FIRST" | "LAST") "ORDER" "BY"
		{expression (ascending_or_descending)?
			("NULLS" ("FIRST" | "LAST"))?, ","}
		")"
		("OVER" "(" query_partition_clause ")" )?
	)?;

aggregate_function_name :
	  "AVG"
	| "COUNT"
	| "CORR"
	| "COVAR_POP"
	| "COVAR_SAMP"
	| "CUME_DIST"
	| "DENSE_RANK"
	| "GROUP_ID"
	| "GROUPING"
	| "GROUPING_ID"
	| "MAX"
	| "MIN"
	| "PERCENTILE_CONT"
	| "PERCENTILE_DISC"
	| "PERCENT_RANK"
	| "RANK"
	| "REGR_SLOPE"
	| "REGR_INTERCEPT"
	| "REGR_COUNT"
	| "REGR_R2"
	| "REGR_AVGX"
	| "REGR_AVGY"
	| "REGR_SXX"
	| "REGR_SYY"
	| "REGR_SXY"
	| "STDDEV"
	| "STDDEV_POP"
	| "STDDEV_SAMP"
	| "SUM"
	| "VAR_POP"
	| "VAR_SAMP"
	| "VARIANCE"
	;

character_function :
	  trim_function
	| character_function_name "(" ({expression, ","})? ")" ;

character_function_name :
	  "CHR"
	| "CONCAT"
	| "INITCAP"
	| "LOWER"
	| "LPAD"
	| "LTRIM"
	| "NLS_INITCAP"
	| "NLS_LOWER"
	| "NLSSORT"
	| "NLS_UPPER"
	| "REPLACE"
	| "RPAD"
	| "RTRIM"
	| "SOUNDEX"
	| "SUBSTR"
	| "TRANSLATE"
	| "TREAT"
	| "TRIM"
	| "UPPER"
 	;

trim_function :
	"TRIM"
	"(" ("LEADING" | "TRAILING" | "BOTH")?
	    (trim_character)?
	    ("FROM")?
	    trim_source
	")";

	trim_character : numeric | string;
	trim_source : numeric | string;

conversion_function :
	  cast_function
	| conversion_function_name
		"(" ({expression ("USING" ("CHAR_CS" | "NCHAR_CS" ))?, ","})? ")"
		// NOTE: "using" clause only for TRANSLATE function
	;

cast_function :
	"CAST" "("
		(
		  "(" subquery ")"
		| "MULTISET" "(" subquery ")"
		| expression
		)
		"AS" type_name
		")" ;

conversion_function_name :
	  "ASCIISTR"
	| "BIN_TO_NUM"
	| "CHARTOROWID"
	| "COMPOSE"
	| "CONVERT"
	| "DECOMPOSE"
	| "HEXTORAW"
	| "NUMTODSINTERVAL"
	| "NUMTOYMINTERVAL"
	| "RAWTOHEX"
	| "RAWTONHEX"
	| "ROWIDTOCHAR"
	| "ROWIDTONCHAR"
	| "TO_CHAR"
	| "TO_CLOB"
	| "TO_DATE"
	| "TO_DSINTERVAL"
	| "TO_LOB"
	| "TO_MULTI_BYTE"
	| "TO_NCHAR"
	| "TO_NCLOB"
	| "TO_NUMBER"
	| "TO_SINGLE_BYTE"
	| "TO_YMINTERVAL"
	| "TRANSLATE"
	| "UNISTR"
	;

treat_function :
	"TREAT" "(" expression "AS" ("REF")?
		(schema ".")? type ")" ;


value_function :
	"VALUE" "(" correlation_variable ")" ;

miscellaneous_single_row_function :
	  miscellaneous_function_name
	"(" ({expression, ","})? ")"
	;

miscellaneous_function_name :
	  "BFILENAME"
	| "COALESCE"
	| "DECODE"
	| "DEPTH"
	| "DUMP"
	| "EMPTY_BLOB"
	| "EMPTY_CLOB"
	| "EXISTSNODE"
	| "EXTRACT"
	| "EXTRACTVALUE"
	| "GREATEST"
	| "LEAST"
	| "NLS_CHARSET_DECL_LEN"
	| "NLS_CHARSET_ID"
	| "NLS_CHARSET_NAME"
	| "NULLIF"
	| "NVL"
	| "NVL2"
	| "PATH"
	| "SYS_CONNECT_BY_PATH"
	| "SYS_CONTEXT"
	| "SYS_DBURIGEN"
	| "SYS_EXTRACT_UTC"
	| "SYS_GUID"
	| "SYS_TYPEID"
	| "SYS_XMLAGG"
	| "SYS_XMLGEN"
	| "UID"
	| "UPDATEXML"
	| "USER"
	| "USERENV"
	| "VSIZE"
	;

analytic_function :
	analytic_function_name
	"(" (arguments)? ")"
	"OVER" "(" analytic_clause ")" ;

analytic_function_name : plsql_identifier;

analytic_clause :
	(query_partition_clause)?
	(order_by_clause (windowing_clause)?)? ;

query_partition_clause :
	"PARTITION" "BY" {value_expr, ","} ;

windowing_clause :
	("ROWS" | "RANGE")
	( "BETWEEN"
	  ( "UNBOUNDED" "PRECEDING"
	  | "CURRENT" "ROW"
	  | value_expr ( "PRECEDING" | "FOLLOWING" )

	  )
	  "AND"
	  ( "UNBOUNDED" "FOLLOWING"
	  | "CURRENT" "ROW"
	  | value_expr ( "PRECEDING" | "FOLLOWING" )
	  )
	| ( "UNBOUNDED" "PRECEDING"
	  | "CURRENT" "ROW"
	  | value_expr "PRECEDING"
	  )
	) ;

object_access_expression :
	(   table_alias "." column "."
	  | object_table_alias "."
	  | "(" expression ")" "." )
	( {attribute, "."} ("." method "(" ({argument, ","})? ")" )?
	| method "(" ({argument, ","})? ")"
	);

type_constructor_expression :
	("NEW")? (schema".")?
	type_name "(" {expression, ","} ")" ;

variable_expression :
	":" host_variable
	(("INDICATOR")? ":" indicator_variable)? ;

object_reference_function :
	object_reference_function_name
	"(" ({expression, ","})? ")" ;

object_reference_function_name :
	  "DEREF"
	| "REF"
	| "VALUE"
	| "MAKE_REF"
	| "REFTOHEX" ;

user_defined_function :
	(schema ".")?
	(   (package ".")? function
	  | user_defined_operator )
	("@" dblink ".")?
	("(" (distinct_or_all)? {expression, ","} ")" )? ;

datatype :
	  Oracle_built_in_datatypes
	| ANSI_supported_datatypes
	| user_defined_types
	| "XMLTYPE"
	;

Oracle_built_in_datatypes :
	  character_datatypes
	| number_datatypes
	| long_and_raw_datatypes
	| datetime_datatypes
	| large_object_datatypes
	| rowid_datatypes
	;

character_datatypes :
	character_datatype_name
  	("(" size ("BYTE" | "CHAR")? ")")? ;

character_datatype_name :
	  "CHAR"
	| "VARCHAR2"
	| "VARCHARC"
	| "NCHAR"
	| "NVARCHAR2"
	;

number_datatypes :
	"NUMBER"
	("(" precision ("," scale)? ")")? ;

long_and_raw_datatypes :
	"LONG" ("RAW" ("(" size ")")?)?
	| ("RAW" | "VARRAW" | "VARRAWC") ("(" size ")")?
	;

datetime_datatypes :
	  "DATE"
	| "TIMESTAMP"
		("(" fractional_seconds_precision ")")?
		("WITH" ("LOCAL")? "TIME" "ZONE")?
	| "INTERVAL" "YEAR"
		("(" year_precision ")")? "TO" "MONTH"
	| "INTERVAL" "DAY"
		("(" day_precision ")")? "TO" "SECOND"
		("(" fractional_seconds_precision ")")?
	;

large_object_datatypes :
	  "BLOB"
	| "CLOB"
	| "NCLOB"
	| "BFILE"
	;

rowid_datatypes :
	  "ROWID"

	| "UROWID" ("(" size ")")?
	;

ANSI_supported_datatypes :
	(
	  "CHARACTER" ("VARYING")?
	| ("CHAR" | "NCHAR") "VARYING"
	| "VARCHAR"
	| "NATIONAL" ("CHARACTER" | "CHAR") ("VARYING")?
	| "NUMERIC" | "DECIMAL" | "DEC"
	| "INTEGER" | "INT" | "SMALLINT"

	| "FLOAT"
	| "DOUBLE" "PRECISION"
	| "REAL")
	("EXTERNAL" ^("NAME" | "VARIABLE"))?
	("(" size ")")?
	;

user_defined_types :
	("REF")? qualified_name
	("%" ("TYPE" | "ROWTYPE"))?
	("(" size ")")?;

	size 			: numeric ("," numeric)?;
	precision 		: numeric;
	day_precision 	: numeric;
	scale 		: numeric;
	fractional_seconds_precision : numeric;
	year_precision 	: numeric;

inline_constraint :
	("CONSTRAINT" constraint_name)?
	((not_tag)? null_tag
	| "UNIQUE" ^"("
	| "PRIMARY KEY"
	| references_clause
	| "CHECK" "(" condition ")"
	) (constraint_state)? ;

out_of_line_constraint :
	("CONSTRAINT" constraint_name)?
	( unique_columns_spec
	| "PRIMARY" "KEY" column_list
	| "FOREIGN" "KEY" column_list references_clause
	| "CHECK" "(" condition ")"
	) (constraint_state)? ;

inline_ref_constraint :
	  "SCOPE" "IS" (schema "." )? scope_table
	| "WITH" "ROWID"
	| ("CONSTRAINT" constraint_name)?
	  references_clause (constraint_state)?
	;

out_of_line_ref_constraint :
	  "SCOPE" "FOR" "(" (ref_col | ref_attr) ")"
	  "IS" ( schema "." )? scope_table
	| "REF" "(" (ref_col | ref_attr) ")" "WITH" "ROWID"
	| ( "CONSTRAINT" constraint_name )?
	      "FOREIGN" "KEY" "(" (ref_col | ref_attr) ")"
	      references_clause ( constraint_state )?
	;

references_clause :
	"REFERENCES" qualified_name (column_list)?
	(on_delete_clause)?
	(constraint_state)? ;

on_delete_clause :
	"ON" "DELETE" (cascade_tag | set_null_tag) ;

	set_null_tag : "SET" "NULL";

constraint_state :
	(
		((not_tag)? "DEFERRABLE")?
		("INITIALLY" ("IMMEDIATE" | "DEFERRED"))?
		((not_tag)? "DEFERRABLE")?
	)?
	("RELY" | "NORELY")?
	(using_index_clause)?
	(enable_or_disable ^"CONSTRAINT")?
	( "VALIDATE" | "NOVALIDATE" )?
	(exceptions_clause)? ;

drop_cluster :
	"DROP" "CLUSTER" qualified_name
	("INCLUDING" "TABLES" (cascade_constraints_tag)?)?;

drop_database_link :
	"DROP" ("PUBLIC")? "DATABASE" "LINK" dblink;

drop_directory :
	"DROP" "DIRECTORY" directory_name;

drop_dimension :
	"DROP" "DIMENSION" qualified_name ;

drop_function :
	"DROP" "FUNCTION" qualified_name;

drop_index :
	"DROP" "INDEX" qualified_name (force_tag)?;

drop_indextype :
	"DROP" "INDEXTYPE" qualified_name (force_tag)?;

drop_context :
	"DROP" "CONTEXT" namespace;

drop_library :
	"DROP" "LIBRARY" library_name;

drop_operator :
	"DROP" "OPERATOR" qualified_name (force_tag)?;

drop_package :
	"DROP" "PACKAGE" ("BODY")? qualified_name ;

drop_procedure :
	"DROP" "PROCEDURE" qualified_name ;

drop_profile :
	"DROP" "PROFILE" profile_name (cascade_tag)?;

drop_role :
	"DROP" "ROLE" role ;

drop_rollback_segment :
	"DROP" "ROLLBACK" "SEGMENT" rollback_segment;

drop_sequence :
	"DROP" "SEQUENCE" qualified_name;

drop_snapshot :
	"DROP" "SNAPSHOT" ("LOG" "ON")? qualified_name;

drop_synonym :
	"DROP" ("PUBLIC")? "SYNONYM"
	synonym_name (force_tag)?;

drop_table :
	"DROP" "TABLE" qualified_name
	(cascade_constraints_tag)?;

drop_tablespace :
	"DROP" tablespace_spec
	("INCLUDING" "CONTENTS" ("AND" "DATAFILES")?
	(cascade_constraints_tag)?)?;

drop_trigger :
	"DROP" "TRIGGER" qualified_name ;

drop_type :
	"DROP" "TYPE" qualified_name
	("FORCE" | "VALIDATE")?;

drop_type_body :
	"DROP" "TYPE" "BODY" qualified_name;

drop_user :
	"DROP" "USER" user_name
	(cascade_tag)?;

drop_view :
	"DROP" "VIEW" qualified_name
	(cascade_constraints_tag)?;

drop_materialized_view :
	"DROP" "MATERIALIZED" "VIEW"
	(schema ".")? ^"LOG" materialized_view;

drop_materialized_view_log :
	"DROP" "MATERIALIZED" "VIEW" "LOG" "ON"
	(schema ".")? table ;

drop_outline :
	"DROP" "OUTLINE" outline ;

raise_statement :
	"RAISE" (exception_name)?;

return_statement :
	"RETURN" ("(" expression ")" | expression )?;

goto_statement :
	"GOTO" label_name;

if_statement :
	"IF" expression "THEN" ({statement})?
	({"ELSIF" expression "THEN" ({statement})?})?
	("ELSE" ({statement})?)?
	("ENDIF" | "END" "IF") ;


loop_statement :
	  basic_loop_statement
	| while_loop_statement
	| for_loop_statement
	;

basic_loop_statement :
	("<<" label_name ">>")?
	"LOOP" ({statement})?

	"END" "LOOP" (label_name)?;

while_loop_statement :
	("<<" label_name ">>")?
	"WHILE" expression
	"LOOP" ({statement})?
	"END" "LOOP" (label_name)?;

for_loop_statement :
	("<<" label_name ">>")?
	"FOR" index_name "IN"
	("REVERSE")? lower_bound (".." upper_bound)?
	"LOOP" ({statement})?
	"END" "LOOP" (label_name)?;

null_statement :
	"NULL" ;

assignment_statement :
	plsql_identifier ":=" expression ;

forall_statement :
	"FORALL" index_name
	"IN" lower_bound ".." upper_bound
	statement;

	index_name : plsql_identifier;
	lower_bound : expression;
	upper_bound : expression;

disassociate_statistics :
	"DISASSOCIATE" "STATISTICS" "FROM"
	{associated_item {qualified_name, ","}}
	(force_tag)? ;

associated_item :
	  "COLUMNS"
	| "FUNCTIONS"
	| "PACKAGES"
	| "TYPES"
	| "INDEXES"
	| "INDEXTYPES"
	;

force_tag : "FORCE" ;

case_statement :
	("<<" label_name ">>")?
	"CASE" (case_operand)?
	{"WHEN" expression "THEN" {statement (";")?}}
	("ELSE" {statement (";")?})?
	"END" "CASE" ( label_name )?;

case_operand : expression;

close_statement :
	"CLOSE" (":")? name ;

fetch_statement :
	"FETCH" (":")? name
	("INTO" ({variable_name,","} | record_name)
	| "BULK" "COLLECT" "INTO"
	  {collection_name | ":" host_array_name, ","}
	  ("LIMIT" expression)?)
	;

exit_statement :
	"EXIT" (label_name)?
	("WHEN" expression)?;

execute_statement :
	"EXECUTE" statement ;

execute_immediate_statement :
	"EXECUTE" "IMMEDIATE" dynamic_string
	("INTO" ({define_variable, ","} | record_name))?
	("USING" {("IN" | "OUT" | "IN" "OUT")? bind_argument, ","})?
	(("RETURNING" | "RETURN") "INTO" {bind_argument, ","})? ;

	dynamic_string : expression ;
	define_variable : plsql_identifier;

explain_plan_statement :
	"EXPLAIN" "PLAN"
	("SET" "STATEMENT_ID" "=" string)?
	("INTO" qualified_name ("@" dblink)?)?
	"FOR" statement;

open_statement :
	"OPEN" cursor_name
	("(" {cursor_parameter_name,","} ")" )?;

open_for_statement :
	"OPEN" (":")? name
	"FOR" select_statement;

///////////////////////////////////////////////////////////////////////

keywords :
	(
	  "ADD"
	| "ALTER"
	| "AUDIT"
	| "AUTOMATIC"
	| "BEGIN"
	| "BODY"
	| "BY"
	| "CALL"
	| "CASE"
	| "CAST"
	| "CHARACTER"
	| "CHECK"
	| "CLEAR"
	| "CLOB"
	| "COMMENT"
	| "COMMIT"
	| "COMPUTE"
	| "CONNECT"
	| "CONSTANT"
	| "CONSTRAINT"
	| "CREATE"
	| "CUBE"
	| "CURSOR"
	| "DATABASE"
	| "DATAFILE"
	| "DECLARE"
	| "DEFAULT"
	| "DELETE"
	| "DISABLE"
	| "DROP"
	| "ELSE"
	| "ELSIF"
	| "ENABLE"
	| "END"
	| "ENDIF"
	| "ESTIMATE"
	| "EXCEPTION"
	| "FETCH"
	| "FOR"
	| "FORALL"
	| "FROM"
	| "FUNCTION"
	| "GLOBAL_NAME"
	| "GRANT"
	| "GROUP"
	| "IF"
	| "IN"
	| "INDEX"
	| "INITIAL"
	| "INITRANS"
	| "INSERT"
	| "INTO"
	| "KEY"
	| "LINK"
	| "LOB"
	| "LOCAL"
	| "LOGFILE"
	| "LOOP"
	| "MAXTRANS"
	| "MEMBER"
	| "MERGE"
	| "MODIFY"
	| "NEXT"
	| "NOAUDIT"
	| "NOSORT"
	| "OPEN"
	| "OPERATOR"
	| "ORDER"
	| "OVER"
	| "PACKAGE"
	| "PARTITION"
	| "PCTFREE"
	| "PRAGMA"
	| "PRIMARY"
	| "PROCEDURE"
	| "PROFILE"
	| "REBUILD"
	| "RECOVER"
	| "REF"
	| "RENAME"
	| "RETURN"
	| "RETURNING"
	| "REVOKE"
	| "ROLLBACK"
	| "ROLLUP"
	| "ROLE"
	| "SCOPE"
	| "SELECT"
	| "SEQUENCE"
	| "SESSION"
	| "SET"
	| "SETS"
	| "START"
	| "STORAGE"
	| "SYNONYM"
	| "TABLE"
	| "TABLESPACE"
	| "THE"
	| "THEN"
	| "TRIGGER"
	| "TYPE"
	| "UNIQUE"
	| "UNTIL"
	| "USER"
	| "UPDATE"
	| "VALUE"
	| "VALUES"
	| "WHEN"
	| "WHERE"
	| "WHILE"
	| "WITH"
	// xml keywords
	| "XMLELEMENT"
	| "XMLTYPE"

	)
	| conversion_function_name
	| miscellaneous_function_name
	;

dblink :
	link_database ("." {domain, "."})?
	("@" connect_descriptor)? ;


	link_database : identifier ;

expression_list :
	  {expression, ","}
	| "(" {expression, ","} ")" ;

ascending_or_descending :
	  "ASC"
	| "DESC"
	;

size_spec :
	"SIZE" integer (K_or_M)? ;

K_or_M :
	"K" | "M" ;

reuse_tag :
	"REUSE" ;

reuse_or_drop_storage :
	("REUSE" | "DROP") "STORAGE" ;

reuse_settings :
	"REUSE" "SETTINGS" ;

compile_clause :
	"COMPILE" (debug_tag)? ;

debug_tag :
	"DEBUG" ;

unusable_tag :
	"UNUSABLE" ;

coalesce_tag :
	"COALESCE" ;

cascade_tag :
	"CASCADE" ;

cascade_constraints_tag :
	"CASCADE" "CONSTRAINTS" ;

enable_or_disable :
	  "ENABLE"
	| "DISABLE" ;

validate_or_novalidate :
	  "VALIDATE"
	| "NOVALIDATE" ;

distinct_or_all :
	  "DISTINCT"
	| "ALL" ;

distinct_all_or_unique :
	  "DISTINCT"
	| "ALL"
	| "UNIQUE" ;

online_or_offline :
	  "ONLINE"
	| "OFFLINE" ;

online_tag :
	"ONLINE" ;

rowdependencies_spec :
	  "NOROWDEPENDENCIES"
	| "ROWDEPENDENCIES" ;

monitoring_spec :
	  "MONITORING"
	| "NOMONITORING" ;

archivelog_spec :
	  "ARCHIVELOG"
	| "NOARCHIVELOG" ;

resetlogs_spec :
	  "RESETLOGS"
	| "NORESETLOGS" ;

from_session :
	"FROM" "SESSION" ;

cache_clause :
	("CACHE" (integer | "READS")?
	 | "NOCACHE")
	(logging_clause)? ;

unusable_local_indexes_clause :
	(rebuild_tag)? "UNUSABLE" "LOCAL" "INDEXES" ;

end_backup_tag :
	"END" "BACKUP" ;

qualified_name :
	plsql_identifier
	("." {identifier, "."})?
	("." string)?
	("#")? ;

identifier :
	"(a-zA-Z_)?(a-zA-Z0-9#_)?*";

not_tag : "NOT";

null_tag :
	"NULL";

compute_statistics_tag :
	"COMPUTE" "STATISTICS" ;

update_block_references_tag :
	"UPDATE" "BLOCK" "REFERENCES" ;

rebuild_tag :
	"REBUILD" ;

overriding_tag :
	"OVERRIDING" ;

final_tag :
	"FINAL" ;

instantiable_tag :
	"INSTANTIABLE" ;

default_tag	:
	"DEFAULT" ;

number :
	("+" | "-")?
	(
	  (numeric ".")? numeric
	  | "." numeric
	  | numeric )
	("E" numeric)? ;

hex_number :
	"x" "(0-9A-F)?+";

string :
	  quotedstring
	| squotedstring;

squotedstring :
	"'" { *("''" | "'"), "''", 0 } "'" ;

name :
	identifier | string;

plsql_identifier :
	"(a-zA-Z_)?(a-zA-Z0-9_$#)?*" (? #VALUE !: keywords";" );

sort_list : {plsql_identifier, ","};

parameters : {parameter,","};

parameter : expression ("BY" "REFERENCE")?;

sqlj_object_type_attr :
	  squotedstring
	| plsql_identifier;

OraData	:
	  squotedstring
	| plsql_identifier;

CustomDatum	:
	  squotedstring
	| plsql_identifier;

column :
	qualified_name ;

function_name
	: qualified_name;

savepoint_name	: plsql_identifier;
directory_object_name : plsql_identifier;
filename		: squotedstring;
field_name		: plsql_identifier;
external_name		: string;
directory_name	: identifier;
segment_name		: identifier;
subtype_name 		: plsql_identifier;
base_type  		: plsql_identifier;
constant_name		: plsql_identifier;
record_name		: plsql_identifier;
record_type_name	: plsql_identifier;
cursor_variable_name: plsql_identifier;
db_table_name		: plsql_identifier;
parameter_name	: identifier;
schema_name		: plsql_identifier;
attribute_name	: plsql_identifier;
scalar_datatype_name: plsql_identifier;
object_type_name	: plsql_identifier;
object_name		: identifier | string;
collection_name	: plsql_identifier;
procedure_name	: identifier;
profile_name		: identifier;
snapshot_name		: identifier;
old_name		: identifier;
cursor_name : expression;
variable_name			: qualified_name;
host_variable_name 	: plsql_identifier;
host_array_name	: plsql_identifier;
exception_name			: qualified_name;
method_name		: squotedstring | plsql_identifier;
lib_name		: squotedstring | plsql_identifier;
constraint_name	: plsql_identifier;
library_name		: identifier;
user_name		: plsql_identifier;
trigger_name		: plsql_identifier;
label_name		: identifier;
query_name  		: plsql_identifier;
type_name		: plsql_identifier;
procedure		: plsql_identifier;
implementation_type 		: qualified_name;
role			: identifier | string ;
password		: identifier | string ;
old_password 		: identifier | string ;
rollback_segment	: identifier;
sequence		: identifier;
collection_item	: identifier;
varray 			: identifier;
proxy 			: identifier | string;
scope_table		: plsql_identifier;
ref_col			: plsql_identifier;
ref_attr		: plsql_identifier;
savepoint		: name;
schema			: plsql_identifier;
table			: plsql_identifier;
view			: plsql_identifier;
materialized_view	: plsql_identifier;
c_alias			: quotedstring | plsql_identifier;
t_alias			: plsql_identifier;
partition		: plsql_identifier;
subpartition		: plsql_identifier;
constraint				: qualified_name ("@" dblink)?;
type			: plsql_identifier;
host_variable 			: qualified_name;
indicator_variable 	: plsql_identifier;
domain 			: plsql_identifier;
connect_descriptor 	: plsql_identifier;
table_alias 		: plsql_identifier;
object_table_alias 	: plsql_identifier;
method 			: plsql_identifier;
database		: plsql_identifier | string;
tablespace 		: plsql_identifier;
LOB_segname 		: plsql_identifier;
correlation_variable: plsql_identifier;
alias 			: plsql_identifier;
object_type 		: plsql_identifier;
index			: plsql_identifier;
cluster 		: identifier;
storage_table 	: plsql_identifier;
varray_item 		: plsql_identifier;
LOB_item 		: plsql_identifier;
log_group 		: plsql_identifier;
access_driver_type 	: plsql_identifier;
directory 		: plsql_identifier;
nested_item 		: plsql_identifier;
package 		: plsql_identifier;
indextype 		: plsql_identifier;
supertype 		: plsql_identifier;
cursor_parameter_name 	: plsql_identifier;

attribute  			: expression;
value 			: expression;
value_expr 			: expression;
single_row_expression 	: expression;
multiple_row_expression : expression;
condition_spec 		: expression;
column_expression 	: expression;
parameter_value 		: expression;
partition_value 		: expression;
comparison_expr 		: expression;
return_expr 		: expression;
else_expr 			: expression;
collection_expression 	: expression;

scalar_subquery_expression : subquery;

plsql_subprogram_body 	: plsql_block;
plsql_function_body 	: plsql_block;
subprogram_body 		: plsql_block;
function_body 		: plsql_block;

alter_mapping_table_clauses 	: "TODO:";
modify_LOB_storage_parameters : "TODO:";
mapping_table_clauses 		: "TODO: mapping_table_clauses";
segment_attribute_clause 	: "TODO: segment_attribute_clause";
user_defined_operator 		: "TODO: user_defined_operator";
sys_refcursor_instance 		: "TODO: sys_refcursor_instance";

fmt 				: squotedstring;
search_condition 		: condition;
attribute_type 		: datatype;
path_string 		: string ;
element 			: string;
size_limit 			: numeric;
numeric_literal 		: numeric;

integer		: numeric;
correlation_integer 	: numeric;
sample_percent 		: numeric;
position 			: numeric;
levels 			: numeric;
min_value 			: numeric;
max_value 			: numeric;
num_buckets 		: numeric;
quantity 			: numeric;
limit 			: numeric;
hash_partition_quantity : numeric;
hash_subpartition_quantity : numeric;

///////////////////////////////////////////////////////////////////

space_symbol  :
		{ ("\32" | "\r" |"\n"|"\t")
		  | "--" *("\n")
   		  |  "/*" *("*/") "*/" ,0 }
   		  ;



sqlj_object_type :
	"EXTERNAL" "NAME" java_ext_name
	"LANGUAGE" "JAVA" "USING"
	(SQLData | CustomDatum | OraData)
	;

sqlj_object_type_sig :
	"EXTERNAL" ("VARIABLE" "NAME" java_static_field_name
         	| "NAME" java_method_sig ) ;


	java_static_field_name : squotedstring ;
	java_method_sig 	     : squotedstring ;


java_ext_name		: squotedstring | plsql_identifier;



///////////////////////////
// xml

xml_function :
	  XMLAgg
	| XMLColAttVal
	| XMLConcat
	| XMLElement
	| XMLForest
	| XMLSequence
	| XMLTransform
	| "XMLTYPE" ("." identifier)?
	  "(" ({expression, ","})? ")"
	;

XMLType_table :
	"CREATE" "TABLE"
	(schema ".")? table
	"OF" "XMLTYPE"
	(XMLType_storage)?
	(XMLSchema_spec)?;

XMLType_column_properties :
	"XMLTYPE" ("COLUMN")? column
	(XMLType_storage)? (XMLSchema_spec)?;

XMLType_storage :
	  "STORE" "AS"
	( "OBJECT" "RELATIONAL"
	| "CLOB"
	    (LOB_segname ("(" LOB_parameters ")")?
	    	| "(" LOB_parameters ")")? );

XMLSchema_spec :
	("XMLSCHEMA" XMLSchema_URL)?
	"ELEMENT" (element
		| XMLSchema_URL "#" element)
	(with_object_id_clause)?
	;

XMLType_view_clause :
	"OF" "XMLTYPE"
	(XMLType_storage)?
	(XMLSchema_spec)?;

XMLAgg:
	"XMLAGG" "(" XMLType_instance
		("ORDER" "BY" sort_list)? ")" ;

XMLElement :
	"XMLELEMENT"
	"(" ("NAME")? expression ("," XML_attributes_clause)?
		("," {value_expr,","})? ")" ;


XML_attributes_clause:
	"XMLATTRIBUTES"
	  "(" {value_expr ("AS" c_alias)?, ","} ")" ;

XMLColAttVal:
	"XMLCOLATTVAL" "(" {value_expr
		("AS" plsql_identifier)?, ","} ")" ;


XMLConcat:
	"XMLCONCAT" "(" {XMLType_instance, ","} ")" ;

XMLForest:
	"XMLFOREST"
	  "(" {value_expr ("AS" c_alias)?, ","} ")" ;

XMLSequence:
	"XMLSEQUENCE" "("
	(XMLType_instance | sys_refcursor_instance ("," fmt )?)
      ")" ;

XMLTransform:
	"XMLTRANSFORM" "(" XMLType_instance
		"," XMLType_instance ")" ;

XMLType_instance :
	  "XMLTYPE" "(" expression ")"
	| expression ;

XMLSchema_URL : string;


///////////////////////////////////////////////
// java

alter_java_statement :
	"ALTER" "JAVA" source_or_class
	(schema ".")? object_name
	("RESOLVER"
	  "(" {"(" match_string (",")? (schema_name | "-") ")"} ")"
	)?
	(   compile_or_resolve
	  | invoker_rights_clause
	) ;

create_java :
	"CREATE" ("OR" "REPLACE")?
	("AND" ("RESOLVE" | "COMPILE"))? ("NOFORCE")? "JAVA"
	(("SOURCE" | "RESOURCE") "NAMED" (schema ".")? primary_name
	| "CLASS" ("SCHEMA" schema)?
	)
	(invoker_rights_clause)?
	("RESOLVER"
	 "(" {"(" match_string (",")? (schema_name | "-") ")"
	")"}
	)?
	("USING"
	  ("BFILE" "(" directory_object_name "," server_file_name ")"
	  | ("CLOB" | "BLOB" | "BFILE") subquery
	  | key_for_BLOB
	  )
	| "AS" java_source_text
	)
	;

drop_java_statement :
	"DROP" "JAVA" ("SOURCE" | "CLASS" | "RESOURCE")
	(schema ".")? object_name;

source_or_class :
	  "SOURCE"
	| "CLASS" ;

compile_or_resolve :
	  "COMPILE"
	| "RESOLVE" ;

match_string :
	string | "*";

java_source_text :
	{*("{" | ";") java_code_block} *(";");

	java_code_block : "{" ({*("{" | "}") (java_code_block)?})? "}";

key_for_BLOB : string;
primary_name : plsql_identifier | string;
server_file_name : string;

class MyParser2 extends Lexer;

DML_table_expression_clause :
	( (schema ".")?
	  ( table
	    ( ("PARTITION" "(" partition ")"
	    | "SUBPARTITION" "(" subpartition ")" )
	    | "@" dblink
	    )?
	  | view ("@" dblink)?
	  )
	| ("THE")? "(" subquery (subquery_restriction_clause)? ")"
	| table_collection_expression
	) ;
