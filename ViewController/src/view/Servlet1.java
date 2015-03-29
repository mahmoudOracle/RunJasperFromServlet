package view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.sql.Connection;
import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import model.AMs.AppModuleAMImpl;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;

import oracle.jbo.client.Configuration;
public class Servlet1 extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PreparedStatement st = null;

        String amDef = "model.AMs.AppModuleAM";
        String config = "AppModuleAMLocal";

        AppModuleAMImpl am = (AppModuleAMImpl) Configuration.createRootApplicationModule(amDef, config);
        st = am.getDBTransaction().createPreparedStatement("select 1 from dual", 0);
        Connection conn = null;

        String reportName = request.getParameter("rn");
        String id = request.getParameter("id");

        try {
            conn = st.getConnection();
        } catch (SQLException e) {
            // TODO: Add catch code
            System.out.println("error 1 getDBTransaction jasper : " + e.getMessage());
        }

        InputStream input =
            new FileInputStream(new File(getServletConfig().getServletContext().getRealPath("/Reports/" + reportName +
                                                                                            ".jrxml")));
        JasperDesign design = null;

        try {
            design = JRXmlLoader.load(input);
        } catch (JRException e) {
            // TODO: Add catch code
            System.out.println("error 2 JasperDesign JRXmlLoader : " + e.getMessage());
        }

        JasperReport report = null;

        try {
            report = JasperCompileManager.compileReport(design);
            report = (JasperReport) JRLoader.loadObject(input);
            report.setWhenNoDataType(WhenNoDataTypeEnum.NO_DATA_SECTION);
        } catch (JRException e) {
            // TODO: Add catch code
            System.out.println("error 3 JasperReport JasperCompileManager.compileReport No Data : " + e.getMessage());
        }

        // Set Parm
        Map parm = new HashMap();
        parm.put("pDept", id); // DeptNo Name Parm Jasper Report

        JasperPrint jasperPrint = null;

        try {
            jasperPrint = JasperFillManager.fillReport(input, parm, conn);
        } catch (JRException e) {
            // TODO: Add catch code
            System.out.println("error 4 JasperPrint JasperFillManager.fillReport : " + e.getMessage());
        }

        // System.out.println("Report Created...");
        OutputStream ouputStream = response.getOutputStream();

        // Report PDF Format
        response.setContentType("application/pdf");
        response.setHeader("Cache-Control", "max-age=0");

        /* JRExporter exporter = new JRPdfExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, ouputStream);

        try {
            exporter.exportReport();
        } catch (JRException e) {
            System.out.println("error 6 JRExporter exporter formats : " + e.getMessage());
            throw new ServletException(e);
        } */
        
        JRPdfExporter exporter = new JRPdfExporter();

        exporter.setExporterInput((ExporterInput) jasperPrint);
        exporter.setExporterOutput((OutputStreamExporterOutput) ouputStream);
        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        exporter.setConfiguration(configuration);

        try {
            exporter.exportReport();
        } catch (JRException e) {
        }finally {
            if (ouputStream != null) {
                try {
                    ouputStream.flush();
                    ouputStream.close();

                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                    throw (ex);
                }
            }
        }
        // /jasperservlet?rn=allData&id=0
        // /jasperservlet?rn=parm&id=value
        // /jasperservlet?rn=#{requestContext.rightToLeft ? 'report arabic' : 'report english'}&id=0
    }
}
