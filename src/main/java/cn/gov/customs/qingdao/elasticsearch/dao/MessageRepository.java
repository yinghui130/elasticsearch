package cn.gov.customs.qingdao.elasticsearch.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;
import cn.gov.customs.qingdao.elasticsearch.pojo.MessageInfo;

@Component
public interface MessageRepository extends ElasticsearchRepository<MessageInfo,String> {
}
