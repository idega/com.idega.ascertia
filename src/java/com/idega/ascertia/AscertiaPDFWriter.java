package com.idega.ascertia;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.idega.io.DownloadWriter;
import com.idega.presentation.IWContext;
import com.idega.util.FileUtil;

public class AscertiaPDFWriter extends DownloadWriter{

	public static final String PARAM_CONVERSATION_ID = "conversation_id";

	private static final Logger logger = Logger.getLogger(AscertiaPDFWriter.class.getName());

	private InputStream inputStream;

	@Override
	public void init(HttpServletRequest req, IWContext iwc) {
		if (iwc == null || !iwc.isLoggedOn()) {
			return;
		}

		AscertiaData ascertiaData = (AscertiaData) iwc.getSession().getAttribute(AscertiaConstants.PARAM_ASCERTIA_DATA);

		byte[] document = ascertiaData.getByteDocument();
		ByteArrayInputStream bais = new ByteArrayInputStream(document);
		this.inputStream = bais;

		setAsDownload(iwc, ascertiaData.getDocumentName(), document.length);
	}

	@Override
	public void writeTo(IWContext iwc, OutputStream streamOut) throws IOException {
		if (inputStream == null) {
			logger.log(Level.SEVERE, "Unable to get input stream");
			return;
		}

		FileUtil.streamToOutputStream(inputStream, streamOut);

		streamOut.flush();
		streamOut.close();
	}
}