package cn.gov.customs.qingdao.elasticsearch.pojo;

import lombok.Data;

@Data
public class QueryBody {

  private String keyword;
  private String appCode;
  private String field;
  private String catalogCode;
  private String begDate;
  private String endDate;
  private String keyword2;
  private int pageIndex;
  private int pageSize;
  private String sortBy;
}
