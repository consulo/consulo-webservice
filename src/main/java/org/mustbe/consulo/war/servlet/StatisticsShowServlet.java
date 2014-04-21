package org.mustbe.consulo.war.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.mustbe.consulo.war.model.StatisticEntry;
import org.mustbe.consulo.war.util.HibernateUtil;

/**
 * @author VISTALL
 * @since 21.04.14
 */
public class StatisticsShowServlet extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>Statistics</title><body>");

		Session session = HibernateUtil.getSessionFactory().openSession();
		try
		{
			session.beginTransaction();

			List list = session.createCriteria(StatisticEntry.class).list();
			session.getTransaction().commit();

			for(Object o : list)
			{
				StatisticEntry statisticEntry = (StatisticEntry) o;
				out.println("User: " + statisticEntry.getUUID() + "<br>");
				for(Map.Entry<String, Long> entry : statisticEntry.getValues().entrySet())
				{
					out.println("<span>&nbsp;&nbsp;&nbsp;&nbsp;</span>" + entry.getKey() + " = " + entry.getValue() + "<br>");
				}
			}

		}
		finally
		{
			session.close();
		}
		out.println("</body></html>");
		out.close();
	}
}
