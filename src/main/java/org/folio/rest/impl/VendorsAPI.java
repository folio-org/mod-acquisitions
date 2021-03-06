package org.folio.rest.impl;

import java.util.List;
import java.util.Map;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import javax.ws.rs.core.Response;

import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Vendor_;
import org.folio.rest.jaxrs.model.Vendors;
import org.folio.rest.jaxrs.resource.VendorsResource;
import org.folio.rest.persist.MongoCRUD;
import org.folio.rest.tools.messages.Messages;
import org.folio.rest.tools.utils.OutStream;
import org.folio.rest.utils.Consts;

import io.vertx.core.Future;

public class VendorsAPI implements VendorsResource {

  private final Messages messages = Messages.getInstance();

  /**
   * Return the 10001 error message.
   * @param lang  - language code for the language the error message should have
   * @return the message
   */
  private String err(String lang) {
    return messages.getMessage(lang, "10001");
  }

  /**
   * Return the 10008 error message.
   * @param lang  - language code for the language the error message should have
   * @return the message
   */
  private String notFound(String lang) {
    return messages.getMessage(lang, "10008");
  }

  private void futureGetVendorsOK(Handler<AsyncResult<Response>> async, Vendors vendors) {
    async.handle(Future.succeededFuture(GetVendorsResponse.withJsonOK(vendors)));
  }

  private void futureGetVendorsError(Exception e, Handler<AsyncResult<Response>> async, String lang) {
    e.printStackTrace();
    async.handle(Future.succeededFuture(GetVendorsResponse.withPlainInternalServerError(err(lang))));
  }

  @Validate
  @Override
  public void getVendors(String query, String orderBy,
      Order order, int offset, int limit, String lang, Map<String, String>okapiHeaders,
      Handler<AsyncResult<Response>> async, Context vertxContext)
      throws Exception {

    vertxContext.runOnContext(v -> {
      try {
        MongoCRUD.getInstance(vertxContext.owner()).get(
          MongoCRUD.buildJson(Vendor_.class.getName(), Consts.VENDORS_COLLECTION, query, orderBy, order, offset, limit),
            reply -> {
              try {
                Vendors vendors = new Vendors();
                List<Vendor_> vendorsList = (List<Vendor_>)reply.result();
                vendors.setVendors(vendorsList);
                vendors.setTotalRecords(vendorsList.size());
                futureGetVendorsOK(async, vendors);
              } catch (Exception e) {
                futureGetVendorsError(e, async, lang);
              }
            });
      } catch (Exception e) {
        futureGetVendorsError(e, async, lang);
      }
    });

  }

  private void futurePostVendorsCreated(Handler<AsyncResult<Response>> async, String result, Vendor_ vendor) {
    String id = result;
    vendor.setId(id);
    OutStream stream = new OutStream();
    stream.setData(vendor);
    async.handle(Future.succeededFuture(PostVendorsResponse.withJsonCreated("vendors/" + id, stream)));
  }

  private void futurePostVendorsError(Exception e, Handler<AsyncResult<Response>> async, String lang) {
    e.printStackTrace();
    async.handle(Future.succeededFuture(PostVendorsResponse.withPlainInternalServerError(err(lang))));
  }

  @Validate
  @Override
  public void postVendors(String lang, Vendor_ vendor, Map<String, String>okapiHeaders,
      Handler<AsyncResult<Response>> async, Context vertxContext)
      throws Exception {

    try {
      vertxContext.runOnContext(v -> {
        try {
          MongoCRUD.getInstance(vertxContext.owner())
              .save(Consts.VENDORS_COLLECTION, vendor,
                  reply -> {
                    try {
                      futurePostVendorsCreated(async, reply.result(), vendor);
                    } catch (Exception e) {
                      futurePostVendorsError(e, async, lang);
                    }
                  });
        } catch (Exception e) {
          futurePostVendorsError(e, async, lang);
        }
      });
    } catch (Exception e) {
      futurePostVendorsError(e, async, lang);
    }
  }

  private void futureGetVendorByIdOK(Handler<AsyncResult<Response>> async, Vendor_ vendor) {
    async.handle(Future.succeededFuture(GetVendorsByVendorIdResponse.withJsonOK(vendor)));
  }

  private void futureGetVendorByIdNotFound(Handler<AsyncResult<Response>> async, String lang) {
    async.handle(Future.succeededFuture(GetVendorsByVendorIdResponse.withPlainNotFound(notFound(lang))));
  }

  private void futureGetVendorByIdError(Exception e, Handler<AsyncResult<Response>> async, String lang) {
    e.printStackTrace();
    async.handle(Future.succeededFuture(GetVendorsByVendorIdResponse.withPlainInternalServerError(err(lang))));
  }

  @Validate
  @Override
  public void getVendorsByVendorId(String vendorId,
      String lang, Map<String, String>okapiHeaders, Handler<AsyncResult<Response>> async,
      Context vertxContext) throws Exception {

    try {
      JsonObject q = new JsonObject().put("_id", vendorId);
      vertxContext.runOnContext(v ->
        MongoCRUD.getInstance(vertxContext.owner()).get(
          MongoCRUD.buildJson(Vendor_.class.getName(), Consts.VENDORS_COLLECTION, q),
            reply -> {
              try {
                List<Vendor_> vendors = (List<Vendor_>)reply.result();
                if (vendors.isEmpty()) {
                  futureGetVendorByIdNotFound(async, lang);
                } else {
                  futureGetVendorByIdOK(async, vendors.get(0));
                }
              } catch (Exception e) {
                futureGetVendorByIdError(e, async, lang);
              }
            })
      );
    } catch (Exception e) {
      futureGetVendorByIdError(e, async, lang);
    }
  }

  private void futureDeleteVendorError(Exception e, Handler<AsyncResult<Response>> async, String lang) {
    e.printStackTrace();
    async.handle(Future.succeededFuture(DeleteVendorsByVendorIdResponse.withPlainInternalServerError(err(lang))));
  }

  private void futureDeleteVendorOK(Handler<AsyncResult<Response>> async) {
    async.handle(Future.succeededFuture(DeleteVendorsByVendorIdResponse.withNoContent()));
  }

  @Validate
  @Override
  public void deleteVendorsByVendorId(String vendorId,
      String lang, Map<String, String>okapiHeaders, Handler<AsyncResult<Response>> async,
      Context vertxContext) throws Exception {
    try {
      vertxContext.runOnContext(v ->
        MongoCRUD.getInstance(vertxContext.owner()).delete(Consts.VENDORS_COLLECTION, vendorId,
            reply -> {
              try {
                futureDeleteVendorOK(async);
              } catch (Exception e) {
                futureDeleteVendorError(e, async, lang);
              }
            })
      );
    } catch (Exception e) {
      futureDeleteVendorError(e, async, lang);
    }
  }

  private void futurePutVendorsByIdError(Exception e, Handler<AsyncResult<Response>> async, String lang) {
    e.printStackTrace();
    async.handle(Future.succeededFuture(PutVendorsByVendorIdResponse.withPlainInternalServerError(err(lang))));
  }

  private void futurePutVendorByIdOK(Handler<AsyncResult<Response>> async) {
    async.handle(Future.succeededFuture(PutVendorsByVendorIdResponse.withNoContent()));
  }

  private void futurePutVendorByIdNotFound(Handler<AsyncResult<Response>> async, String entity) {
    async.handle(Future.succeededFuture(PutVendorsByVendorIdResponse.withPlainNotFound(entity)));
  }

  @Validate
  @Override
  public void putVendorsByVendorId(String vendorId,
      String lang, Vendor_ entity, Map<String, String>okapiHeaders,
      Handler<AsyncResult<Response>> async, Context vertxContext)
      throws Exception {

    try {
      JsonObject q = new JsonObject().put("_id", vendorId);
      vertxContext.runOnContext(v ->
        MongoCRUD.getInstance(vertxContext.owner()).update(Consts.VENDORS_COLLECTION, entity, q,
            reply -> {
              if(reply.succeeded() && reply.result().getDocMatched() == 0){
                futurePutVendorByIdNotFound(async, vendorId);
              }
              else{
                try {
                  futurePutVendorByIdOK(async);
                } catch (Exception e) {
                  futurePutVendorsByIdError(e, async, lang);
                }
              }
            })
      );
    } catch (Exception e) {
      futurePutVendorsByIdError(e, async, lang);
    }
  }

}
