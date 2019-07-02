package cn.gov.customs.qingdao.elasticsearch.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;
import cn.gov.customs.qingdao.elasticsearch.pojo.StepInfo;

@Component
public interface StepRespository extends ElasticsearchRepository<StepInfo, String> {
  
}
