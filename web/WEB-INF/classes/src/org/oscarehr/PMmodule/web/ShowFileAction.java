package org.oscarehr.PMmodule.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import com.ibm.io.file.exception.FileExistsException;
import com.ibm.io.file.exception.FileNotFoundException;
import com.quatro.model.Attachment;
import com.quatro.model.AttachmentText;
import com.quatro.model.LookupCodeValue;
import com.quatro.service.LookupManager;
import com.quatro.service.UploadFileManager;

public class ShowFileAction extends BaseAction {
	private static Log log = LogFactory.getLog(UploadFileAction.class);

	private UploadFileManager uploadFileManager;

	private LookupManager lookupManager;

	public void setUploadFileManager(UploadFileManager uploadFileManager) {
		this.uploadFileManager = uploadFileManager;
	}

	public ActionForward unspecified(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Integer recordId = Integer.parseInt((String) request.getParameter("id"));
		Attachment attObj = uploadFileManager.getAttachmentDetail(recordId);
		AttachmentText attTextObj = uploadFileManager.getAttachmentText(recordId);
		LookupCodeValue lcv = lookupManager.GetLookupCode("DTT", attObj.getFileType());
		String contentType = lcv.getDescription();

		response.setContentType(contentType);
		Integer len = attTextObj.getAttData().length;
		response.addHeader("Content-Length", len.toString());
		response.addHeader("Content-Disposition", "filename=\""
				+ attObj.getFileName() + "\"; size=" + len.toString());
		OutputStream o = response.getOutputStream();
		o.write(attTextObj.getAttData());
		o.flush();
		o.close();
		return null;
	}

	public void image(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException, Exception {

	}

	private void sendFile(Integer docId, String fileName,
			HttpServletRequest req, byte[] data)
	{
		String appPath = req.getContextPath();
		String workFolder = req.getServletPath();
		workFolder = workFolder + "\\SHELTER" + docId.toString();
		File file = new File(workFolder, fileName);

		if (!file.exists()) {
			try {
				OutputStream bos = new FileOutputStream(file);
				bos.write(data);
				bos.flush();
				bos.close();
				bos = null;

				req.setAttribute("sFilePath", file.getPath());

			} catch (Exception ex) {
				;
			}
		}

	}

	public void setLookupManager(LookupManager lookupManager) {
		this.lookupManager = lookupManager;
	}
}
