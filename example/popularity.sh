#!/bin/sh
curl -s -XDELETE "http://localhost:9200/test"
echo
curl -s -XPUT "http://localhost:9200/test/" -d '{
    "settings": {
        "index.number_of_shards": 1,
        "index.number_of_replicas": 0
    },
    "mappings": {
        "type1": {
            "properties": {
                "name": {
                    "type": "string"
                },
                "number": {
                    "type": "integer"
                },
                "searchterm_pins": {
                    "type": "string"
                },
                "twitter_shares": {
                    "type": "integer",
                    "null_value": 0
                },
                "page_views": {
                    "type": "integer",
                    "null_value": 0
                }
            }
        }
    }
}'
echo
curl -s -XPUT "localhost:9200/test/type1/1" -d '{"name" : "foo bar baz", "searchterm_pins": "blah" }'
curl -s -XPUT "localhost:9200/test/type1/2" -d '{"name" : "foo foo foo" }'
curl -s -XPUT "localhost:9200/test/type1/3" -d '{"name" : "packers rule foo", "page_views": 0 }'
curl -s -XPUT "localhost:9200/test/type1/4" -d '{"name" : "bears suck foo", "twitter_shares": 10000 }'
curl -s -XPOST "http://localhost:9200/test/_refresh"
echo
curl -s "localhost:9200/test/type1/_search?pretty=true" -d '{
    "query": {
        "function_score" : {
            "boost_mode": "replace",
            "query": {
                "match": {
                    "name": "foo"
                }
            },
            "script_score": {
                "script": "castlepopularity",
                "lang": "native",
                "params": {
                    "search": "blah"
                }
            }
        }
    }
}
'
echo

curl -s "localhost:9200/test/type1/_search?pretty=true" -d '{
    "query": {
        "function_score" : {
            "boost_mode": "replace",
            "query": {
                "match": {
                    "name": "foo"
                }
            },
            "script_score": {
                "script": "castlepopularity",
                "lang": "native",
                "params": {
                    "search": "blah"
                }
            }
        }
    }
}
'
echo
