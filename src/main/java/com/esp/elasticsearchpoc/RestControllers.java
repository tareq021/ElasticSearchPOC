package com.esp.elasticsearchpoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/esp")
public class RestControllers {

    private static final String INDEX ="documents";
    private final RestHighLevelClient restHighLevelClient;
    private final ObjectMapper objectMapper;

    private Utilities utilities = new Utilities();

    @Autowired
    public RestControllers(RestHighLevelClient restHighLevelClient, ObjectMapper objectMapper) {
        this.restHighLevelClient = restHighLevelClient;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/create-index")
    public ResponseEntity createIndex() throws IOException {
        List<Document> documents = utilities.readExcel();

        return ResponseEntity.ok(createIndexes(documents));
    }

    private long createIndexes(List<Document> documents) throws IOException {
        BulkRequest request = new BulkRequest();
        for (Document document: documents){
            Map<String, Object> documentMapper = objectMapper.convertValue(document, Map.class);
            UUID uuid = UUID.randomUUID();
            documentMapper.put("id", uuid.toString());
            documentMapper.put("count", document.getCount());
            documentMapper.put("isbn", document.getIsbn());
            documentMapper.put("authors", document.getAuthors());
            documentMapper.put("publishingYear", document.getPublishingYear());
            documentMapper.put("originalTitle", document.getOriginalTitle());
            documentMapper.put("title", document.getTitle());
            documentMapper.put("language", document.getLanguage());
            documentMapper.put("averageRating", document.getAverageRating());

            request.add(new IndexRequest(INDEX).id(uuid.toString())
                    .source(documentMapper));
        }

        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);

        return bulkResponse.getItems().length;
    }

    @GetMapping("/find-by-Id/{id}")
    public Document findById(@PathVariable String id) throws IOException {
        GetRequest getRequest = new GetRequest(INDEX, id);

        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        Map<String, Object> resultMap = getResponse.getSource();

        return objectMapper
                .convertValue(resultMap, Document.class);
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam String term) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.multiMatchQuery(term, "title", "authors"));
        sourceBuilder.from(0);
        sourceBuilder.size(500);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(INDEX);
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits searchHits = searchResponse.getHits();

        List<Document> documents = new ArrayList<>();
        for (SearchHit searchHit:searchHits){
            Map<String, Object> resultMap = searchHit.getSourceAsMap();
            documents.add(objectMapper
                    .convertValue(resultMap, Document.class));
        }

        return documents;
    }
}
