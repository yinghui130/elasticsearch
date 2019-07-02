package cn.gov.customs.qingdao.elasticsearch.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
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
import cn.gov.customs.qingdao.elasticsearch.dao.MessageRepository;
import cn.gov.customs.qingdao.elasticsearch.pojo.MessageInfo;

@Service
public class MessageService {
  private ElasticsearchTemplate template;
  private MessageRepository repository;

  public MessageService(ElasticsearchTemplate template, MessageRepository repository) {
    this.template = template;
    this.repository = repository;
  }

  public MessageInfo save(MessageInfo info) {
    MessageInfo result = this.repository.save(info);

    return result;
  }

  public Iterable<MessageInfo> insertList(List<MessageInfo> list) {
    Iterable<MessageInfo> result = this.repository.saveAll(list);
    return result;
  }

  public Page<MessageInfo> doQuery(String keyword, String field, String appCode, String catalogCode,
      String begDate, String endDate, String keyword2, Pageable pageable) {
    NativeSearchQueryBuilder queryBuilder =
        new NativeSearchQueryBuilder().withIndices(ElasticSearchConsts.MESSAGE_INDEX_NAME)
            .withTypes(ElasticSearchConsts.MESSAGE_TYPE_NAME).withSearchType(SearchType.DEFAULT)
            .withPageable(pageable);

    BoolQueryBuilder builder = QueryBuilders.boolQuery();

    if ("default".equals(field)) {
      builder.must(QueryBuilders.multiMatchQuery(keyword, "msgName", "content"));
      if (!StringUtils.isEmpty(keyword2)) {
        builder.must(QueryBuilders.multiMatchQuery(keyword2, "msgName", "content"));
      }

      // queryBuilder.withHighlightFields(
      // new HighlightBuilder.Field("msgName").preTags("<strong>").postTags(("</strong>")),
      // new HighlightBuilder.Field("content").fragmentSize(200).preTags("<strong>")
      // .postTags(("</strong>")));
    } else {
      if (!StringUtils.isEmpty(field)) {
        if (!StringUtils.isEmpty(keyword))
          builder = builder.must(QueryBuilders.matchQuery(field, keyword));
        if (!StringUtils.isEmpty(keyword2))
          builder = builder.must(QueryBuilders.matchQuery(field, keyword2));

        // queryBuilder.withHighlightFields(new HighlightBuilder.Field(field).fragmentSize(200)
        // .preTags("<strong>").postTags(("</strong>")));
      }
    }

    if (!StringUtils.isEmpty(catalogCode))
      builder.must(QueryBuilders.matchQuery("catalogCode", catalogCode));
    if (!StringUtils.isEmpty(appCode))
      builder.must(QueryBuilders.matchQuery("appCode", appCode));
    if (!StringUtils.isEmpty(begDate) && !StringUtils.isEmpty(endDate)) {
      builder.filter(QueryBuilders.rangeQuery("createTime").from(begDate).to(endDate));
    }

    SearchQuery query = queryBuilder.withQuery(builder).build();

    Page<MessageInfo> page =
        this.template.queryForPage(query, MessageInfo.class, new SearchResultMapper() {
          @SuppressWarnings("unchecked")
          @Override
          public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz,
              Pageable pageable) {
            List<MessageInfo> result =
                "default".equals(field) ? getList(response, "msgName", "content")
                    : getList(response, field);

            if (result.size() > 0) {
              return new AggregatedPageImpl<T>((List<T>) result, pageable,
                  response.getHits().getTotalHits(), response.getScrollId());
            }

            return new AggregatedPageImpl<T>((List<T>) result);
          }
        });

    return page;
  }

  private List<MessageInfo> getList(SearchResponse response, String... fields) {
    List<MessageInfo> result = Lists.newArrayList();
    SearchHits hits = response.getHits();

    if (hits.getHits().length <= 0) {
      return result;
    }

    for (SearchHit searchHit : hits) {
      MessageInfo info = new MessageInfo();
      info.setMsgId(searchHit.getId());
      info.setCatalogCode(searchHit.getSourceAsMap().get("catalogCode").toString());
      info.setAppCode(searchHit.getSourceAsMap().get("appCode").toString());
      info.setContent(searchHit.getSourceAsMap().get("content").toString());
      info.setMsgName(searchHit.getSourceAsMap().get("msgName").toString());
      DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
      String createTime =
          df.format(LocalDateTime.parse(searchHit.getSourceAsMap().get("createTime").toString()));
      info.setCreateTime(createTime);

      result.add(info);
    }

    return result;
  }
}
