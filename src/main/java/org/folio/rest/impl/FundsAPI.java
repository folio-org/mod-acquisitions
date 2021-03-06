package org.folio.rest.impl;

import java.util.List;
import java.util.Map;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import javax.ws.rs.core.Response;

import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Fund;
import org.folio.rest.jaxrs.model.Funds;
import org.folio.rest.jaxrs.resource.FundsResource;
import org.folio.rest.utils.Consts;
import org.folio.rest.persist.MongoCRUD;
import org.folio.rest.tools.utils.OutStream;
import org.folio.rest.tools.messages.MessageConsts;
import org.folio.rest.tools.messages.Messages;

public class FundsAPI implements FundsResource {

  private static final Logger log = LoggerFactory.getLogger(FundsAPI.class);
  private final Messages messages = Messages.getInstance();

  @Validate
  @Override
  public void getFunds(String query, String orderBy, Order order, int offset, int limit, String lang,
      Map<String, String>okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    System.out.println("sending... getFunds");
    vertxContext.runOnContext(v -> {
      try {
        MongoCRUD.getInstance(vertxContext.owner()).get(
          MongoCRUD.buildJson(Fund.class.getName(), Consts.FUNDS_COLLECTION, query, orderBy, order, offset, limit),
            reply -> {
              try {
                Funds funds = new Funds();
                // this is wasteful!!!
                List<Fund> fundObj = (List<Fund>)reply.result();
                funds.setFunds(fundObj);
                funds.setTotalRecords(fundObj.size());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsResponse.withJsonOK(funds)));
              } catch (Exception e) {
                log.error(e);
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsResponse.withPlainInternalServerError(messages
                    .getMessage(lang, MessageConsts.InternalServerError))));
              }
            });
      } catch (Exception e) {
        log.error(e);
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsResponse.withPlainInternalServerError(messages.getMessage(
            lang, MessageConsts.InternalServerError))));
      }
    });

  }

  @Validate
  @Override
  public void postFunds(String lang, Fund fund, Map<String, String>okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) throws Exception {

    try {
      System.out.println("sending... postFunds");
      vertxContext.runOnContext(v -> {

        try {
          MongoCRUD.getInstance(vertxContext.owner())
              .save(Consts.FUNDS_COLLECTION, fund,
                  reply -> {
                    try {
                      String id = reply.result();
                      fund.setId(id);
                      OutStream stream = new OutStream();
                      stream.setData(fund);
                      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostFundsResponse.withJsonCreated(
                          "funds/" + id, stream)));
                    } catch (Exception e) {
                      log.error(e);
                      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostFundsResponse
                          .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
                    }
                  });
        } catch (Exception e) {
          log.error(e);
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostFundsResponse.withPlainInternalServerError(messages
              .getMessage(lang, MessageConsts.InternalServerError))));
        }
      });
    } catch (Exception e) {
      log.error(e);
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostFundsResponse.withPlainInternalServerError(messages.getMessage(
          lang, MessageConsts.InternalServerError))));
    }
  }

  @Validate
  @Override
  public void getFundsByFundId(String fundId, String lang, Map<String, String>okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) throws Exception {

    try {
      JsonObject q = new JsonObject();
      q.put("_id", fundId);
      System.out.println("sending... getFundsByFundId");
      vertxContext.runOnContext(v -> {
        MongoCRUD.getInstance(vertxContext.owner()).get(
          MongoCRUD.buildJson(Fund.class.getName(), Consts.FUNDS_COLLECTION, q),
            reply -> {
              try {
                List<Fund> funds = (List<Fund>)reply.result();
                if (funds.size() == 0) {
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsByFundIdResponse.withPlainNotFound("Patron"
                      + messages.getMessage(lang, "10008"))));
                } else {
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsByFundIdResponse.withJsonOK(funds.get(0))));
                }
              } catch (Exception e) {
                log.error(e);
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsByFundIdResponse
                    .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
              }
            });
      });
    } catch (Exception e) {
      log.error(e);
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsByFundIdResponse.withPlainInternalServerError(messages
          .getMessage(lang, MessageConsts.InternalServerError))));
    }
  }

  @Validate
  @Override
  public void deleteFundsByFundId(String fundId, String lang, Map<String, String>okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) throws Exception {

    try {
      JsonObject q = new JsonObject();
      q.put("_id", fundId);
      System.out.println("sending... deleteFundsByFundId");
      vertxContext.runOnContext(v -> {
        MongoCRUD.getInstance(vertxContext.owner()).delete(Consts.FUNDS_COLLECTION, fundId,
            reply -> {
              try {
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteFundsByFundIdResponse.withNoContent()));
              } catch (Exception e) {
                log.error(e);
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteFundsByFundIdResponse
                    .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
              }
            });
      });
    } catch (Exception e) {
      log.error(e);
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteFundsByFundIdResponse.withPlainInternalServerError(messages
          .getMessage(lang, MessageConsts.InternalServerError))));
    }
  }

  @Validate
  @Override
  public void putFundsByFundId(String fundId, String lang, Fund entity,
      Map<String, String>okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    try {
      JsonObject q = new JsonObject();
      q.put("_id", fundId);
      System.out.println("sending... putPatronsByPatronId");
      vertxContext.runOnContext(v -> {
        MongoCRUD.getInstance(vertxContext.owner()).update(Consts.FUNDS_COLLECTION, entity, q,
            reply -> {
              if(reply.succeeded() && reply.result().getDocMatched() == 0){
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutFundsByFundIdResponse.
                  withPlainNotFound(fundId)));
              }
              else{
                try {
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutFundsByFundIdResponse.withNoContent()));
                } catch (Exception e) {
                  log.error(e);
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutFundsByFundIdResponse
                      .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
                }
              }
            });
      });
    } catch (Exception e) {
      log.error(e);
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutFundsByFundIdResponse.withPlainInternalServerError(messages
          .getMessage(lang, MessageConsts.InternalServerError))));
    }

  }

}
