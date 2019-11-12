# Vert.x ElasticSearch Service

Vert.x 3 elasticsearch service with event bus proxying and RxJava support. 
Originaly Forked from [ef-labs/vertx-elasticsearch-service](https://github.com/ef-labs/vertx-elasticsearch-service) but deverged a lot since then. It's a seperate library now for ES. 

### Version Matrix

| vert.x    | elasticsearch  | vertx-elasticsearch-service     |
| --------- | -------------- | ---------------------------     |
| 3.3.3     | 2.2.2          | 1.0.0                           |
| 3.3.3     | 2.2.2          | 1.1.0                           |
| 3.5.0     | 5.6.1          | 2.0.0                           |
| 3.5.0     | 6.1.1          | 2.2.0                           |
| 3.5.0     | 6.1.1          | 2.3.0                           |


## Compatibility
- Java 8+
- Vert.x 3.x.x

## Dependencies

### Maven
```xml
<dependency>
    <groupId>com.hubrick.vertx</groupId>
    <artifactId>vertx-elasticsearch-service</artifactId>
    <version>2.3.0</version>
</dependency>
```


## Configuration

Deploying the vertx service with id 'com.hubrick.vertx.vertx-elasticsearch-service'. The configuration options are as follows:

```json
{
    "address": <address>,
    "transportAddresses": [ { "hostname": <hostname>, "port": <port> } ],
    "cluster_name": <cluster_name>,
    "client_transport_sniff": <client_transport_sniff>
}
```

* `address` - The event bus address to listen on.  (Required)
* `transportAddresses` - An array of transport address objects containing `hostname` and `port`.  If no transport address are provided the default is `"localhost"` and `9300`
    * `hostname` - the ip or hostname of the node to connect to.
    * `port` - the port of the node to connect to.  The default is `9300`.
* `cluster_name` - the elastic search cluster name.  The default is `"elasticsearch"`.
* `client_transport_sniff` - the client will sniff the rest of the cluster and add those into its list of machines to use.  The default is `true`.

An example configuration would be:

```json
{
    "address": "eb.elasticsearch",
    "transportAddresses": [ { "hostname": "host1", "port": 9300 }, { "hostname": "host2", "port": 9301 } ],
    "cluster_name": "my_cluster",
    "client_transport_sniff": true
}
```

NOTE: No configuration is needed if running elastic search locally with the default cluster name.


#### Dependency Injection

The `DefaultElasticSearchService` requires a `TransportClientFactory` and `ElasticSearchConfigurator` to be injected.

Default bindings are provided for HK2 and Guice, but you can create your own bindings for your container of choice.

See the [englishtown/vertx-hk2](https://github.com/englishtown/vertx-hk2) or [englishtown/vertx-guice](https://github.com/englishtown/vertx-guice) projects for further details.


## Commands

### Index

https://www.elastic.co/guide/reference/api/index_/

```java
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    final IndexOptions indexOptions = new IndexOptions()
        .setId("123")
        .setOpType(IndexRequest.OpType.INDEX)
        .setTtl(100000l);
        // etc.
    
    elasticSearchService.index("twitter", "tweet", new JsonObject().put("user", "hubrick").put("message", "love elastic search!"), indexOptions, indexResponse -> {
        // Do something
    });
    
    
    // RxJava 1
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    // RxJava 2
    final Rx2ElasticSearchService rx2ElasticSearchService = Rx2ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    final IndexOptions indexOptions = new IndexOptions()
        .setId("123")
        .setOpType(IndexRequest.OpType.INDEX)
        .setTtl(100000l);
        // etc.
        
    rxElasticSearchService.index("twitter", "tweet", new JsonObject().put("user", "hubrick").put("message", "love elastic search!"), indexOptions)
        .subscribe(indexResponse -> {
            // Do something
        });
```

### Get

https://www.elastic.co/guide/reference/api/get/

```java
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    final GetOptions getOptions = new GetOptions()
        .setFetchSource(true)
        .addField("id")
        .addField("message");
        // etc.
    
    elasticSearchService.get("twitter", "tweet", "123", getOptions, getResponse -> {
        // Do something
    });
    
    
    // RxJava 1
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    // RxJava 2
    final Rx2ElasticSearchService rx2ElasticSearchService = Rx2ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    final GetOptions getOptions = new GetOptions()
        .setFetchSource(true)
        .addField("id")
        .addField("message");
        // etc.
        
    rxElasticSearchService.get("twitter", "tweet", "123", getOptions)
        .subscribe(getResponse -> {
            // Do something
        });
```

### Update

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html

```java
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    final UpdateOptions updateOptions = new UpdateOptions()
        .setScript("ctx._source.field = 'new value'", ScriptType.INLINE);
        // etc.
    
    elasticSearchService.update("twitter", "tweet", "123", updateOptions, updateResponse -> {
        // Do something
    });
    
    
    // RxJava 1
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    // RxJava 2
    final Rx2ElasticSearchService rx2ElasticSearchService = Rx2ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    final UpdateOptions updateOptions = new UpdateOptions()
        .setScript("ctx._source.field = 'new value'", ScriptType.INLINE);
        // etc.
        
    rxElasticSearchService.update("twitter", "tweet", "123", updateOptions)
        .subscribe(updateResponse -> {
            // Do something
        });
```

### Bulk

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html

```java
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    final BulkOptions options = new BulkOptions().setTimeout("5s");
    final BulkIndexOptions bulkIndexOptions = new BulkIndexOptions()
        .setIndex("twitter")
        .setType("tweet")
        .setSource(new JsonObject().put("user", "hubrick").put("message", "love elastic search!"))
        .setIndexOptions(new IndexOptions().setId("1")),
        // etc.
    
    // There are corresponding methods for update (bulkUpdate), delete (bulkeDelete) or combined (bulk)
    elasticSearchService.bulkIndex(Collections.singletonList(bulkIndexOptions), options, bulkResponse -> {
        // Do something
    });
    
    
    // RxJava 1
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    // RxJava 2
    final Rx2ElasticSearchService rx2ElasticSearchService = Rx2ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    final BulkOptions options = new BulkOptions().setTimeout("5s");
    final BulkIndexOptions bulkIndexOptions = new BulkIndexOptions()
        .setIndex("twitter")
        .setType("tweet")
        .setSource(new JsonObject().put("user", "hubrick").put("message", "love elastic search!"))
        .setIndexOptions(new IndexOptions().setId("1")),
        // etc.
        
    // There are corresponding methods for update (bulkUpdate), delete (bulkeDelete) or combined (bulk)
    rxElasticSearchService.bulkIndex(Collections.singletonList(bulkIndexOptions), options)
        .subscribe(bulkResponse -> {
            // Do something
        });
```

### Multi Get

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html

```java
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    final MultiGetQueryOptions multiGetQueryOptions = new MultiGetQueryOptions()
        .setId("id")
        .setIndex("twitter")
        .setType("tweet")
        .setFetchSource(true);
        // etc.
    
    elasticSearchService.multiGet(Collections.singletonList(multiGetQueryOptions), multiGetResponse -> {
        // Do something
    });
    
    
    // RxJava 1
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    // RxJava 2
    final Rx2ElasticSearchService rx2ElasticSearchService = Rx2ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    final MultiGetQueryOptions multiGetQueryOptions = new MultiGetQueryOptions()
        .setId("id")
        .setIndex("twitter")
        .setType("tweet")
        .setFetchSource(true);
        // etc.
        
    rxElasticSearchService.multiGet(Collections.singletonList(multiGetQueryOptions))
        .subscribe(multiGetResponse -> {
            // Do something
        });
```

### Search

https://www.elastic.co/guide/reference/api/search/

https://www.elastic.co/guide/reference/query-dsl/

```java
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    final SearchOptions searchOptions = new SearchOptions()
        .setQuery(new JsonObject("{\"match_all\": {}}"))
        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
        .setFetchSource(true)
        .addFieldSort("id", SortOrder.DESC)
        .addScriptSort("...", ScriptSortOption.Type.NUMERIC, new JsonObject(), SortOrder.DESC);
        // etc.
        
    elasticSearchService.search("twitter", searchOptions, searchResponse -> {
        // Do something
    }); 
    
    
    // RxJava 1
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    // RxJava 2
    final Rx2ElasticSearchService rx2ElasticSearchService = Rx2ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    final SearchOptions searchOptions = new SearchOptions()
        .setQuery(new JsonObject("{\"match_all\": {}}"))
        .setSearchType(SearchType.SCAN)
        .setFetchSource(true)
        .addFieldSort("id", SortOrder.DESC)
        .addScriptSort("...", ScriptSortOption.Type.NUMERIC, new JsonObject(), SortOrder.DESC);
        // etc.
            
    rxElasticSearchService.search("twitter", searchOptions)
        .subscribe(searchResponse -> {
            // Do something
        });
```

### Multi Search

https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html

```java
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    final SearchOptions searchOptions = new SearchOptions()
        .setQuery(new JsonObject("{\"match_all\": {}}"))
        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
        .setFetchSource(true)
        .addFieldSort("id", SortOrder.DESC)
        .addScriptSort("...", ScriptSortOption.Type.NUMERIC, new JsonObject(), SortOrder.DESC);
        // etc.
        
    final MultiSearchQueryOptions multiSearchQueryOptions = new MultiSearchQueryOptions().addIndex("twitter").setSearchOptions(searchOptions);
        
    elasticSearchService.multiSearch(Collections.singletonList(multiSearchQueryOptions), multiSearchResponse -> {
        // Do something
    }); 
    
    // RxJava 1
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    // RxJava 2
    final Rx2ElasticSearchService rx2ElasticSearchService = Rx2ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    final SearchOptions searchOptions = new SearchOptions()
        .setQuery(new JsonObject("{\"match_all\": {}}"))
        .setSearchType(SearchType.SCAN)
        .setFetchSource(true)
        .addFieldSort("id", SortOrder.DESC)
        .addScriptSort("...", ScriptSortOption.Type.NUMERIC, new JsonObject(), SortOrder.DESC);
        // etc.
        
    final MultiSearchQueryOptions multiSearchQueryOptions = new MultiSearchQueryOptions().addIndex("twitter").setSearchOptions(searchOptions);
            
    rxElasticSearchService.multiSearch("twitter", Collections.singletonList(multiSearchQueryOptions))
        .subscribe(multiSearchResponse -> {
            // Do something
        });
```

### Scroll

https://www.elastic.co/guide/reference/api/search/scroll/

First send a search message with `search_type` = `"scan"` and `scroll` = `"5m"` (some time string).  The search result will include a `_scroll_id` that will be valid for the scroll time specified.

```java
{
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    final SearchScrollOptions searchScrollOptions = new SearchScrollOptions()
        .setScroll("5m");

    elasticSearchService.searchScroll("c2Nhbjs1OzIxMTpyUkpzWnBIYVMzbVB0VGlaNHdjcWpnOzIxNTpyUkpzWnBI", searchScrollOptions, searchResponse -> {
        // Do something
    });
    
    // RxJava 1
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    // RxJava 2
    final Rx2ElasticSearchService rx2ElasticSearchService = Rx2ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
            
    final SearchScrollOptions searchScrollOptions = new SearchScrollOptions()
        .setScroll("5m");
    
    rxElasticSearchService.searchScroll("c2Nhbjs1OzIxMTpyUkpzWnBIYVMzbVB0VGlaNHdjcWpnOzIxNTpyUkpzWnBI", searchScrollOptions)
        .subscribe(searchResponse -> {
            // Do something
        });
}
```

### Delete
https://www.elastic.co/guide/reference/api/delete/

```java
{
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
           
    final DeleteOptions deleteOptions = new DeleteOptions()
        .setTimeout("10s");
        
    elasticSearchService.delete("twitter", "tweet", "123", deleteOptions, deleteResponse -> {
        // Do something
    });
    
    // RxJava 1
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    // RxJava 2
    final Rx2ElasticSearchService rx2ElasticSearchService = Rx2ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
           
    final DeleteOptions deleteOptions = new DeleteOptions()
        .setTimeout("10s");
            
    rxElasticSearchService.delete("twitter", "tweet", "123", deleteOptions)
        .subscribe(deleteResponse -> {
            // Do something
        });;
}
```

### Delete By Query

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html

```java
{
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    final DeleteByQueryOptions deleteByQueryOptions = new DeleteByQueryOptions()
        .setTimeoutInMillis(1000l)
        .setQuery(new JsonObject("{\"match_all\": {}}")); 
           
    elasticSearchService.deleteByQuery("twitter", deleteByQueryOptions, deleteByQueryResponse -> {
        // Do something
    });
    
    
    // RxJava 1
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    // RxJava 2
    final Rx2ElasticSearchService rx2ElasticSearchService = Rx2ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    final DeleteByQueryOptions deleteByQueryOptions = new DeleteByQueryOptions()
        .setTimeoutInMillis(1000l)
        .setQuery(new JsonObject("{\"match_all\": {}}")); 
              
    rxElasticSearchService.deleteByQuery("twitter", deleteByQueryOptions)
        .subscribe(deleteByQueryResponse -> {
            // Do something
        });
}
```

