# Cassandra client library

Alpakka Cassandra uses the [DataStax Java Driver for Apache Cassandra](https://github.com/datastax/java-driver) version $cassandra-driver.version$.

For the Java Driver API of package `com.datastax.oss.driver.api` binary compatibility is guaranteed across minor and patch versions. (see [docs](https://github.com/datastax/java-driver/tree/4.x/manual/api_conventions)).

Cassandra identifiers, such as keyspace, table and column names, are case-insensitive by default (see [docs](https://github.com/datastax/java-driver/tree/4.x/manual/case_sensitivity)).

https://github.com/datastax/java-driver/tree/4.x/manual/core/shaded_jar

https://github.com/datastax/java-driver/tree/4.x/manual/osgi


You might be tempted to open a separate session for each keyspace used in your application; however, connection pools are created at the session level, so each new session will consume additional system resources:
https://github.com/datastax/java-driver/tree/4.x/manual/core#cqlsession



https://github.com/datastax/java-driver/tree/4.x/manual/core/configuration

Reference conf
https://github.com/datastax/java-driver/tree/4.x/manual/core/configuration/reference
