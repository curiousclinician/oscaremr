package oscar.oscarDemographic;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.oscarehr.util.DbConnectionFilter;

import oscar.OscarAction;
import oscar.OscarDocumentCreator;


import java.io.File;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.util.JRLoader;

public class PrintDemoLabelAction extends OscarAction {
	
    public PrintDemoLabelAction() {
    }

    public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        //patient
        String classpath = (String)request.getSession().getServletContext().getAttribute("org.apache.catalina.jsp_classpath");
        if (classpath==null) classpath = (String)request.getSession().getServletContext().getAttribute("com.ibm.websphere.servlet.application.classpath");
        
        System.setProperty("jasper.reports.compile.class.path", classpath);

        HashMap parameters = new HashMap();
        parameters.put("demo", request.getParameter("demographic_no"));
        ServletOutputStream sos = null;
        InputStream ins = null;
        try {
            ServletContext context = getServlet().getServletContext();
            ins = getClass().getResourceAsStream("/oscar/oscarDemographic/label.xml");
//            ins = context.getResourceAsStream("/label.xml");
//            ins = new FileInputStream(System.getProperty("user.home") + "/label.xml");
        }
        catch (Exception ex1) {
            ex1.printStackTrace();
        }

        try {
            sos = response.getOutputStream();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        response.setHeader("Content-disposition", getHeader(response).toString());
        OscarDocumentCreator osc = new OscarDocumentCreator();
        try {
            osc.fillDocumentStream(parameters, sos, "pdf", ins, DbConnectionFilter.getThreadLocalDbConnection());
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return actionMapping.findForward(this.target);
    }

    private StringBuffer getHeader(HttpServletResponse response) {
        StringBuffer strHeader = new StringBuffer();
        strHeader.append("label_");
        strHeader.append(".pdf");
        response.setHeader("Cache-Control", "max-age=0");
        response.setDateHeader("Expires", 0);
        response.setContentType("application/pdf");
        StringBuffer sbContentDispValue = new StringBuffer();
        sbContentDispValue.append("inline; filename="); //inline - display
        sbContentDispValue.append(strHeader);
        return sbContentDispValue;
    }
}
