package cn.gov.customs.qingdao.elasticsearch.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import cn.gov.customs.qingdao.elasticsearch.config.ElasticSearchConsts;
import lombok.Data;


/**
 * @author tom
 * 报文类
 */
@Data
@Document(indexName=ElasticSearchConsts.MESSAGE_INDEX_NAME,type=ElasticSearchConsts.MESSAGE_TYPE_NAME)
public class MessageInfo {
  @Id
  private String msgId;
 
  @Field
  private String msgName;
  
  @Field(analyzer="ik_max_word",searchAnalyzer="ik_max_word")
  private String content;
  
  @Field
  private String appCode;
  
  @Field
  private String catalogCode;
  
  @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private String createTime;
}
