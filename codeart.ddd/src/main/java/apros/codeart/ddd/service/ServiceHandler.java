package apros.codeart.ddd.service;

import apros.codeart.UIException;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.mq.TransferData;
import apros.codeart.mq.rpc.server.IRPCHandler;

public class ServiceHandler implements IRPCHandler {

	@Override
	public TransferData process(String method, DTObject args) {
		
		if (!ServiceHost.isEnabled()) 
			throw new UIException(Language.strings("codeart.ddd","StartingService"));

	       DTObject returnValue = null;
	       DTObject status = null;
	       int dataLength = 0;
	       byte[] content = null;

	       try
	       {
	           var request = ServiceRequest.Create(arg);
	           InitIdentity(request);

	           if (request.TransmittedLength == null)
	           {
	               returnValue = ProcessService(request);
	           }
	           else
	           {
	               var result = ProcessDownloadService(request);
	               returnValue = result.Info;
	               dataLength = result.DataLength;
	               content = result.Content;
	           }
	           
	           status = ServiceHostUtil.Success;
	       }
	       catch (Exception ex)
	       {
	           Logger.Fatal(ex);
	           status = ServiceHostUtil.CreateFailed(ex);
	       }

	       var reponse = DTObject.Create();
	       reponse["status"] = status;
	       reponse["returnValue"] = returnValue;
	       return new TransferData(AppSession.Language, reponse, dataLength, content);
	}
	
	
	   protected ServiceHandler() { }

	   private void InitIdentity(ServiceRequest request)
	   {
	       AppSession.Identity = request.Identity;
	   }


	   private DTObject ProcessService(ServiceRequest request)
	   {
	       var provider = ServiceProviderFactory.Create(request);
	       return provider.Invoke(request);
	   }

	   private BinaryData ProcessDownloadService(ServiceRequest request)
	   {
	       var provider = ServiceProviderFactory.Create(request);
	       return provider.InvokeBinary(request);
	   }

	   public static readonly MQServiceHandler Instance = new MQServiceHandler();

}
