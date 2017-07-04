package me.chanjar.weixin.mp.api;

import me.chanjar.weixin.common.session.InternalSession;
import me.chanjar.weixin.common.session.InternalSessionManager;
import me.chanjar.weixin.common.session.StandardSessionManager;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.common.util.LogExceptionHandler;
import me.chanjar.weixin.common.api.WxErrorExceptionHandler;
import me.chanjar.weixin.common.api.WxMessageDuplicateChecker;
import me.chanjar.weixin.common.api.WxMessageInMemoryDuplicateChecker;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * <pre>
 * 微信消息路由器，通过代码化的配置，把来自微信的消息交给handler处理
 * 
 * 说明：
 * 1. 配置路由规则时要按照从细到粗的原则，否则可能消息可能会被提前处理
 * 2. 默认情况下消息只会被处理一次，除非使用 {@link WxMpMessageRouterRule#next()}
 * 3. 规则的结束必须用{@link WxMpMessageRouterRule#end()}或者{@link WxMpMessageRouterRule#next()}，否则不会生效
 * 
 * 使用方法：
 * WxMpMessageRouter router = new WxMpMessageRouter();
 * router
 *   .rule()
 *       .msgType("MSG_TYPE").event("EVENT").eventKey("EVENT_KEY").content("CONTENT")
 *       .interceptor(interceptor, ...).handler(handler, ...)
 *   .end()
 *   .rule()
 *       // 另外一个匹配规则
 *   .end()
 * ;
 * 
 * // 将WxXmlMessage交给消息路由器
 * router.route(message);
 * 
 * </pre>
 * @author Daniel Qian
 *
 */
public class WxMpMessageRouter {

  protected final Logger log = LoggerFactory.getLogger(WxMpMessageRouter.class);

  private static final int DEFAULT_THREAD_POOL_SIZE = 100;

  private final List<WxMpMessageRouterRule> rules = new ArrayList<WxMpMessageRouterRule>();

  private final WxMpService wxMpService;

//  private ExecutorService executorService;

//  private WxMessageDuplicateChecker messageDuplicateChecker;

//  private WxSessionManager sessionManager;

//  private WxErrorExceptionHandler exceptionHandler;

  public WxMpMessageRouter(WxMpService wxMpService) {
    this.wxMpService = wxMpService;
//    this.executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
//    this.messageDuplicateChecker = new WxMessageInMemoryDuplicateChecker();
//    this.sessionManager = new StandardSessionManager();
//    this.exceptionHandler = new LogExceptionHandler();
  }

  public WxMpMessageRouter(WxMpService wxMpService,String temp){
	  this.wxMpService = wxMpService;
  }
  
  

  

  

  List<WxMpMessageRouterRule> getRules() {
    return this.rules;
  }

  /**
   * 开始一个新的Route规则
   * @return
   */
  public WxMpMessageRouterRule rule() {
    return new WxMpMessageRouterRule(this);
  }

  /**
   * 处理微信消息
   * @param wxMessage
   */
  public WxMpXmlOutMessage route(final WxMpXmlMessage wxMessage) {
    if (isDuplicateMessage(wxMessage)) {
      // 如果是重复消息，那么就不做处理
      return null;
    }

    final List<WxMpMessageRouterRule> matchRules = new ArrayList<WxMpMessageRouterRule>();
    // 收集匹配的规则
    for (final WxMpMessageRouterRule rule : rules) {
      if (rule.test(wxMessage)) {
        matchRules.add(rule);
        if(!rule.isReEnter()) {
          break;
        }
      }
    }

    if (matchRules.size() == 0) {
      return null;
    }

    WxMpXmlOutMessage res = null;
    final List<Future> futures = new ArrayList<Future>();
		for (final WxMpMessageRouterRule rule : matchRules) {
			// 返回最后一个非异步的rule的执行结果

			res = rule.service(wxMessage, wxMpService, null,
					null);
			// 在同步操作结束，session访问结束
			log.debug("End session access: async=false, sessionId={}",
					wxMessage.getFromUserName());
//			sessionEndAccess(wxMessage);
		}

    return res;
  }

  protected boolean isDuplicateMessage(WxMpXmlMessage wxMessage) {
	  //接到就处理,不管重复了
//    String messageId = "";
//    if (wxMessage.getMsgId() == null) {
//      messageId = String.valueOf(wxMessage.getCreateTime())
//          + "-" + wxMessage.getFromUserName()
//          + "-" + String.valueOf(wxMessage.getEventKey() == null ? "" : wxMessage.getEventKey())
//          + "-" + String.valueOf(wxMessage.getEvent() == null ? "" : wxMessage.getEvent())
//      ;
//    } else {
//      messageId = String.valueOf(wxMessage.getMsgId());
//    }
//
//    if (messageDuplicateChecker !=null && messageDuplicateChecker.isDuplicate(messageId)) {
//      return true;
//    }
    return false;

  }

  
}
