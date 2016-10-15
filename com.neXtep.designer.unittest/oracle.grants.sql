drop user unittest_target cascade;

create user unittest_target identified by unittest_target;

grant connect, resource, create procedure, create sequence, create materialized view, create cluster, create type, create view, create trigger, create synonym, create public synonym to unittest_target
/

