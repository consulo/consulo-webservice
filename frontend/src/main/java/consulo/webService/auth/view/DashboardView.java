package consulo.webService.auth.view;

import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import consulo.webService.errorReporter.domain.ErrorReport;
import consulo.webService.errorReporter.mongo.ErrorReportRepository;
import consulo.webService.ui.util.TidyComponents;

@SpringView(name = DashboardView.ID)
public class DashboardView extends VerticalLayout implements View
{
	public static final String ID = "";

	@Autowired
	protected ErrorReportRepository myErrorReportRepository;

	public DashboardView()
	{
		setSizeFull();
	}

	private Component buildLastPluginComments()
	{
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setHeight(100, Unit.PERCENTAGE);
		verticalLayout.setWidth(20, Unit.EM);

		verticalLayout.addComponent(new Label("Last Plugin Comments:"));
		verticalLayout.addComponent(TidyComponents.newLabel("TODO"));
		return verticalLayout;
	}

	private Component buildLastSettingsUpdate()
	{
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setHeight(100, Unit.PERCENTAGE);
		verticalLayout.setWidth(20, Unit.EM);

		verticalLayout.addComponent(new Label("Last Settings Update:"));
		verticalLayout.addComponent(TidyComponents.newLabel("TODO"));
		return verticalLayout;
	}

	private Component buildLastErrorReports(Authentication authentication)
	{
		Page<ErrorReport> reportList = myErrorReportRepository.findByReporterEmail(authentication.getName(), new PageRequest(0, 15, new Sort(Sort.Direction.DESC, ErrorReportRepository.CREATE_DATE)));

		VerticalLayout verticalLayout = new VerticalLayout();

		verticalLayout.addComponent(new Label("Last Error Reports:"));

		for(ErrorReport errorReport : reportList)
		{
			HorizontalLayout shortLine = new HorizontalLayout();
			shortLine.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
			shortLine.setWidth(100, Unit.PERCENTAGE);

			VerticalLayout lineLayout = new VerticalLayout();
			lineLayout.setWidth(100, Unit.PERCENTAGE);
			lineLayout.addComponent(shortLine);
			lineLayout.addStyleName("errorViewLineLayout");

			lineLayout.addStyleName("errorViewLineLayout" + StringUtil.capitalize(errorReport.getStatus().name().toLowerCase(Locale.US)));

			HorizontalLayout leftLayout = new HorizontalLayout();
			leftLayout.setWidth(100, Unit.PERCENTAGE);
			leftLayout.setSpacing(true);
			leftLayout.addComponent(TidyComponents.newLabel("Message: " + StringUtil.shortenTextWithEllipsis(errorReport.getMessage(), 30, 10)));
			leftLayout.addComponent(TidyComponents.newLabel("At: " + new Date(errorReport.getCreateDate())));

			shortLine.addComponent(leftLayout);
			shortLine.setComponentAlignment(leftLayout, Alignment.MIDDLE_LEFT);

			verticalLayout.addComponent(lineLayout);
		}

		return verticalLayout;
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		removeAllComponents();

		Label label = new Label("Dashboard");
		label.addStyleName("headerMargin");
		addComponent(label);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null)
		{
			return;
		}

		VerticalSplitPanel panel = new VerticalSplitPanel();
		panel.addStyleName("bodyMargin");
		panel.setSizeFull();
		panel.setSplitPosition(50, Unit.PERCENTAGE);

		addComponent(panel);
		setExpandRatio(panel, 0.9f);

		HorizontalLayout topLayout = new HorizontalLayout();
		topLayout.setSizeFull();
		panel.setFirstComponent(topLayout);

		HorizontalLayout bottomLayout = new HorizontalLayout();
		bottomLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);
		bottomLayout.setSizeFull();

		Component lastPluginComments = buildLastPluginComments();
		bottomLayout.addComponent(lastPluginComments);
		bottomLayout.setExpandRatio(lastPluginComments, 0.33f);

		Component lastSettingsUpdate = buildLastSettingsUpdate();
		bottomLayout.addComponent(lastSettingsUpdate);
		bottomLayout.setExpandRatio(lastSettingsUpdate, 0.33f);

		Component lastErrorReports = buildLastErrorReports(authentication);
		bottomLayout.addComponent(lastErrorReports);
		bottomLayout.setExpandRatio(lastErrorReports, 0.33f);

		panel.setSecondComponent(bottomLayout);
	}
}
