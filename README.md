# Vert.x ElasticSearch Service

Vert.x 3 elasticsearch service with event bus proxying and RxJava support. 
Forked from [ef-labs/vertx-elasticsearch-service](https://github.com/ef-labs/vertx-elasticsearch-service). 

The difference to [ef-labs/vertx-elasticsearch-service](https://github.com/ef-labs/vertx-elasticsearch-service) are some additional features and the request and reponse objects are concrete java classes instead of the generic JsonObject.

### Version Matrix

| vert.x    | elasticsearch  | vertx-elasticsearch-service     |
| --------- | -------------- | ---------------------------     |
| 3.3.3     | 2.2.2          | 1.0.0                           |
| 3.3.3     | 2.2.2          | 1.1.0                           |
| 3.5.0     | 5.6.1          | 1.3.0                           |

## Compatibility
- Java 8+
- Vert.x 3.x.x

## Dependencies

### Maven
```xml
<dependency>
    <groupId>com.hubrick.vertx</groupId>
    <artifactId>vertx-elasticsearch-service</artifactId>
    <version>1.1.0</version>
</dependency>
```


## Configuration

The configuration options are as follows:

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


## Action Commands

### Index

http://www.elasticsearch.org/guide/reference/api/index_/

An example would be:

```java
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    elasticSearchService.index("twitter", "tweet", new JsonObject().put("user", "hubrick").put("message", "love elastic search!"), indexResponse -> {
        // Do something
    });
    
    final IndexOptions indexOptions = new IndexOptions()
        .setId("123")
        .setOpType(IndexRequest.OpType.INDEX)
        .setTtl(100000l);
        // etc.
    
    elasticSearchService.index("twitter", "tweet", new JsonObject().put("user", "hubrick").put("message", "love elastic search!"), indexOptions, indexResponse -> {
        // Do something
    });
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    rxElasticSearchService.index("twitter", "tweet", new JsonObject().put("user", "hubrick").put("message", "love elastic search!"))
        .subscribe(indexResponse -> {
            // Do something
        });
        
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

http://www.elasticsearch.org/guide/reference/api/get/

An example would be:

```java
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    elasticSearchService.get("twitter", "tweet", "123", getResponse -> {
        // Do something
    });
    
    final GetOptions getOptions = new GetOptions()
        .setFetchSource(true)
        .addField("id")
        .addField("message");
        // etc.
    
    elasticSearchService.get("twitter", "tweet", "123", getOptions, getResponse -> {
        // Do something
    });
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    rxElasticSearchService.get("twitter", "tweet", "123")
        .subscribe(getResponse -> {
            // Do something
        });
        
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

### Search

http://www.elasticsearch.org/guide/reference/api/search/

http://www.elasticsearch.org/guide/reference/query-dsl/

An example message would be:

```java
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    elasticSearchService.search("twitter", searchResponse -> {
        // Do something
    });
    
    final SearchOptions searchOptions = new SearchOptions()
        .setQuery(new JsonObject("{\"match_all\": {}}"))
        .setSearchType(SearchType.SCAN)
        .setFetchSource(true)
        .addFieldSort("id", SortOrder.DESC)
        .addScriptSort("...", ScriptSortOption.Type.NUMERIC, new JsonObject(), SortOrder.DESC);
        // etc.
        
    elasticSearchService.search("twitter", searchOptions, searchResponse -> {
        // Do something
    }); 
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    rxElasticSearchService.search("twitter")
        .subscribe(searchResponse -> {
            // Do something
        });
        
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

### Scroll

http://www.elasticsearch.org/guide/reference/api/search/scroll/

First send a search message with `search_type` = `"scan"` and `scroll` = `"5m"` (some time string).  The search result will include a `_scroll_id` that will be valid for the scroll time specified.

An example message would be:

```java
{
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    elasticSearchService.searchScroll("c2Nhbjs1OzIxMTpyUkpzWnBIYVMzbVB0VGlaNHdjcWpnOzIxNTpyUkpzWnBI", searchResponse -> {
        // Do something
    });
    
    final SearchScrollOptions searchScrollOptions = new SearchScrollOptions()
        .setScroll("5m");

    elasticSearchService.searchScroll("c2Nhbjs1OzIxMTpyUkpzWnBIYVMzbVB0VGlaNHdjcWpnOzIxNTpyUkpzWnBI", searchScrollOptions, searchResponse -> {
        // Do something
    });
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
            
    rxElasticSearchService.searchScroll("c2Nhbjs1OzIxMTpyUkpzWnBIYVMzbVB0VGlaNHdjcWpnOzIxNTpyUkpzWnBI")
        .subscribe(searchResponse -> {
            // Do something
        });
       
    final SearchScrollOptions searchScrollOptions = new SearchScrollOptions()
        .setScroll("5m");
    
    rxElasticSearchService.searchScroll("c2Nhbjs1OzIxMTpyUkpzWnBIYVMzbVB0VGlaNHdjcWpnOzIxNTpyUkpzWnBI", searchScrollOptions)
        .subscribe(searchResponse -> {
            // Do something
        });
}
```

### Suggest

http://www.elasticsearch.org/guide/reference/api/search/scroll/

First send a search message with `search_type` = `"scan"` and `scroll` = `"5m"` (some time string).  The search result will include a `_scroll_id` that will be valid for the scroll time specified.

An example message would be:

```java
{
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    elasticSearchService.suggest("twitter", suggestResponse -> {
        // Do something
    });
    
    final SuggestOptions suggestOptions = new SuggestOptions();
    final CompletionSuggestOption completionSuggestOption = new CompletionSuggestOption()
        .setText("the amsterdma meetpu");
        // etc.
    suggestOptions.addSuggestion("my-suggest-1", completionSuggestOption);

    elasticSearchService.suggest("twitter", suggestOptions, suggestResponse -> {
        // Do something
    });
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
            
    rxElasticSearchService.suggest("twitter")
        .subscribe(suggestResponse -> {
            // Do something
        });
        
    final SuggestOptions suggestOptions = new SuggestOptions();
    final CompletionSuggestOption completionSuggestOption = new CompletionSuggestOption()
       .setText("the amsterdma meetpu");
       // etc.
    suggestOptions.addSuggestion("my-suggest-1", completionSuggestOption);
    
    rxElasticSearchService.suggest("twitter", suggestOptions)
        .subscribe(suggestResponse -> {
            // Do something
        });
}
```

### Delete

http://www.elasticsearch.org/guide/reference/api/delete/

An example message would be:

```java
{
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
           
    elasticSearchService.delete("twitter", "tweet", "123", deleteResponse -> {
        // Do something
    });
    
    final DeleteOptions deleteOptions = new DeleteOptions()
        .setTimeout("10s");
        
    elasticSearchService.delete("twitter", "tweet", "123", deleteOptions, deleteResponse -> {
        // Do something
    });
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
           
    rxElasticSearchService.delete("twitter", "tweet", "123")
        .subscribe(deleteResponse -> {
            // Do something
        });
        
    final DeleteOptions deleteOptions = new DeleteOptions()
        .setTimeout("10s");
            
    rxElasticSearchService.delete("twitter", "tweet", "123", deleteOptions)
        .subscribe(deleteResponse -> {
            // Do something
        });;
}
```

## Supported Plugins
### Delete By Query

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html

An example message would be:

```java
{
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    final JsonObject query = new JsonObject("{\"match_all\": {}}");
           
    elasticSearchService.deleteByQuery("twitter", query, deleteByQueryResponse -> {
        // Do something
    });
    
    final DeleteByQueryOptions deleteByQueryOptions = new DeleteByQueryOptions()
        .setTimeout("10s");
        
    elasticSearchService.delete("twitter", query, deleteByQueryOptions, deleteByQueryResponse -> {
        // Do something
    });
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    final JsonObject query = new JsonObject("{\"match_all\": {}}");
              
    rxElasticSearchService.deleteByQuery("twitter", query)
        .subscribe(deleteByQueryResponse -> {
            // Do something
        });
      
    final DeleteByQueryOptions deleteByQueryOptions = new DeleteByQueryOptions()
        .setTimeout("10s");
            
    rxElasticSearchService.delete("twitter", query, deleteByQueryOptions)
        .subscribe(deleteByQueryResponse -> {
            // Do something
        });
}
```
