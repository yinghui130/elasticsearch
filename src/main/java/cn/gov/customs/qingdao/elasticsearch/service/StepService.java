package cn.gov.customs.qingdao.elasticsearch.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;
import cn.gov.customs.qingdao.elasticsearch.config.ElasticSearchConsts;
import cn.gov.customs.qingdao.elasticsearch.dao.StepRespository;
import cn.gov.customs.qingdao.elasticsearch.pojo.StepInfo;

@Service
public class StepService {
  private ElasticsearchTemplate template;
  private StepRespository repository;

  public StepService(ElasticsearchTemplate template, StepRespository repository) {
    this.template = template;
    this.repository = repository;
  }

  public StepInfo save(StepInfo info) {
    StepInfo result = this.repository.save(info);

    return result;
  }

  public Iterable<StepInfo> insertList(List<StepInfo> list) {
    Iterable<StepInfo> result = this.repository.saveAll(list);
    return result;
  }

  public Page<StepInfo> doQuery(String msgName,  Pageable pageable) {
    NativeSearchQueryBuilder queryBuilder =
        new NativeSearchQueryBuilder().withIndices(ElasticSearchConsts.STEP_INDEX_NAME)
            .withTypes(ElasticSearchConsts.STEP_TYPE_NAME).withSearchType(SearchType.DEFAULT)
            .withPageable(pageable);

    BoolQueryBuilder builder = QueryBuilders.boolQuery();

   
    builder.must(QueryBuilders.matchQuery("msgName", msgName));
  
    SearchQuery query = queryBuilder.withQuery(builder).build();
 
    Page<StepInfo> page =
        this.template.queryForPage(query, StepInfo.class, new SearchResultMapper() {
          @SuppressWarnings("unchecked")
          @Override
          public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz,
              Pageable pageable) {
            List<StepInfo> result =getList(response, "msgName");

            if (result.size() > 0) {
              return new AggregatedPageImpl<T>((List<T>) result, pageable,
                  response.getHits().getTotalHits(), response.getScrollId());
            }

            return new AggregatedPageImpl<T>((List<T>) result);
          }
        });

    return page;
  }

  private List<StepInfo> getList(SearchResponse response, String... fields) {
    List<StepInfo> result = Lists.newArrayList();
    SearchHits hits = response.getHits();

    if (hits.getHits().length <= 0) {
      return result;
    }

    for (SearchHit searchHit : hits) {
      StepInfo info = new StepInfo();
      info.setMsgId(searchHit.getId());
      info.setAppCode(searchHit.getSourceAsMap().get("appCode").toString());
      info.setAppName(searchHit.getSourceAsMap().get("appName").toString());
      info.setMsgName(searchHit.getSourceAsMap().get("msgName").toString());
      info.setMsgDesc(searchHit.getSourceAsMap().get("msgDesc").toString());
      DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
      String createTime =
          df.format(LocalDateTime.parse(searchHit.getSourceAsMap().get("createTime").toString()));
      info.setCreateTime(createTime);

      result.add(info);
    }

    return result;
  }
}
