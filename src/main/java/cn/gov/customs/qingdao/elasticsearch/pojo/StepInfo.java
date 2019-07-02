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
 * 报文处理日志类
 */
@Data
@Document(indexName=ElasticSearchConsts.STEP_INDEX_NAME,type=ElasticSearchConsts.STEP_TYPE_NAME)
public class StepInfo {
  @Id
  private String msgId;
  
  @Field(type=FieldType.Keyword, fielddata = true)
  private String msgName;
  
  @Field
  private String msgDesc;
  
  @Field(type=FieldType.Keyword,index=false)
  private String appCode;
  
  @Field
  private String appName;
  
  @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private String createTime;
}
