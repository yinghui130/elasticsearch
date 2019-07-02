package cn.gov.customs.qingdao.elasticsearch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.gov.customs.qingdao.elasticsearch.dao.MessageRepository;
import cn.gov.customs.qingdao.elasticsearch.dao.StepRespository;
import cn.gov.customs.qingdao.elasticsearch.pojo.MessageInfo;
import cn.gov.customs.qingdao.elasticsearch.pojo.QueryBody;
import cn.gov.customs.qingdao.elasticsearch.pojo.StepInfo;
import cn.gov.customs.qingdao.elasticsearch.service.MessageService;
import cn.gov.customs.qingdao.elasticsearch.service.StepService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

@RestController
@CrossOrigin
@RequestMapping("/api/msg")
@Api("MsgController：用于存储报文和处理环节至ElasticSearch的api")
public class MsgController {
  @Autowired
  private StepService stepService;
  @Autowired
  private MessageService messageService;

  @ApiOperation(value = "保存报文信息", notes = "保存报文的内容，创建时间，报文名")
  @ApiImplicitParam(name = "messageInfo", value = "报文", required = true, dataType = "MessageInfo")
  @PostMapping("/saveMessage")
  public HttpEntity<?> saveMessagEntity(@RequestBody MessageInfo messageInfo) {
    this.messageService.save(messageInfo);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @PostMapping("/saveStepInfo")
  @ApiOperation(value = "保存报文处理步骤", notes = "保存报文的报文名，描述，处理应用code，处理的应用名，处理时间(createTime格式:yyyy-MM-ddTHH:mm:ss))")
  @ApiImplicitParam(name = "stepInfo", value = "处理步骤", required = true, dataType = "StepInfo")
  public HttpEntity<?> saveStepEntity(@RequestBody StepInfo stepInfo) {
    this.stepService.save(stepInfo);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @PostMapping("/queryMessage")
  @ApiOperation(value = "查询相关报文消息",
      notes = "根据内容和字段等信息查询,其中begDate和endData格式为yyyy-MM-ddTHH:mm:ss\n" + "参数说明：" + "{\n"
          + "  \"appCode\": \"EXAM0000\",\n"
          + "  \"begDate\": \"2018-10-03T21:56:56（开始时间时间格式 yyyy-MM-ddTHH:mm:ss）\",\n"
          + "  \"catalogCode\": \"exam（报文扩展名）\",\n"
          + "  \"endDate\": \"2018-10-22T21:56:56（开始时间时间格式 yyyy-MM-ddTHH:mm:ss）\",\n"
          + "  \"field\": \"content（此处一般填content即可代表报文内容）\",\n" + "  \"keyword\": \"刘\",\n"
          + "  \"keyword2\": \"\",\n" + "  \"pageIndex\": 0,\n" + "  \"pageSize\": 10,\n"
          + "  \"sortBy\": \"createTime\"\n" + "}")
  @ApiImplicitParam(name = "queryBody", value = "查询报文", required = true, dataType = "QueryBody")
  public HttpEntity<?> queryMessage(@RequestBody QueryBody queryBody) {
    Pageable pageable = "default".equals(queryBody.getSortBy())
        ? PageRequest.of(queryBody.getPageIndex(), queryBody.getPageSize())
        : PageRequest.of(queryBody.getPageIndex(), queryBody.getPageSize(),
            Sort.by(Direction.DESC, queryBody.getSortBy()));
    Page<MessageInfo> page = this.messageService.doQuery(queryBody.getKeyword(),
        queryBody.getField(), queryBody.getAppCode(), queryBody.getCatalogCode(),
        queryBody.getBegDate(), queryBody.getEndDate(), queryBody.getKeyword2(), pageable); // SortBy

    return new ResponseEntity<>(page.getContent(), HttpStatus.OK);
  }

  @PostMapping("/queryStepInfo")
  @ApiOperation(value = "查询报文的处理环节", notes = "根据报文名查询报文处理环节")
  @ApiImplicitParam(name = "msgName", value = "查询报文的处理环节", required = true)
  public HttpEntity<?> queryStep(@RequestBody String msgName) {
    Pageable pageable = PageRequest.of(0, 100, Sort.by(Direction.DESC, "createTime"));
    Page<StepInfo> page = this.stepService.doQuery(msgName, pageable); // SortBy

    return new ResponseEntity<>(page.getContent(), HttpStatus.OK);
  }

  @GetMapping("/test")
  public HttpEntity<?> test() {
    return new ResponseEntity<>("test ok", HttpStatus.OK);
  }
}
