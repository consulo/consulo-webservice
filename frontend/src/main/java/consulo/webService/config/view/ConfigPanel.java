package consulo.webService.config.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.SystemProperties;
import com.vaadin.data.HasValue;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import consulo.webService.UserConfigurationService;
import consulo.webService.github.GithubPropertyKeys;
import consulo.webService.ui.util.TinyComponents;
import consulo.webService.ui.util.VaadinUIUtil;
import consulo.webService.util.GAPropertyKeys;
import consulo.webService.util.PropertyKeys;
import consulo.webService.util.PropertySet;

/**
 * @author VISTALL
 * @since 14-Apr-17
 */
public class ConfigPanel extends VerticalLayout
{
	private final List<Consumer<Properties>> myConsumers = new ArrayList<>();
	private final UserConfigurationService myConfigurationService;

	public ConfigPanel(@Nonnull UserConfigurationService configurationService, @Nonnull String buttonName, @Nonnull Runnable action)
	{
		myConfigurationService = configurationService;
		setSpacing(false);
		setMargin(false);

		VerticalLayout layout = VaadinUIUtil.newVerticalLayout();
		layout.setSizeUndefined();
		layout.setSpacing(true);
		layout.setSizeFull();

		layout.addComponent(buildRepositoryGroup());
		layout.addComponent(buildCaptchaGroup());
		layout.addComponent(buildGithubGroup());
		layout.addComponent(buildGAGroup());

		Button installButton = TinyComponents.newButton(buttonName);
		installButton.addClickListener(event ->
		{
			Properties properties = new Properties();

			for(Consumer<Properties> consumer : myConsumers)
			{
				consumer.accept(properties);
			}

			configurationService.setProperties(properties);

			action.run();
		});
		installButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		layout.addComponent(installButton);
		layout.setComponentAlignment(installButton, Alignment.MIDDLE_RIGHT);

		addComponent(layout);
		setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
	}

	@SuppressWarnings("unchecked")
	private <T> void map(@Nonnull Class<T> clazz, @Nonnull HasValue<T> property, @Nonnull String key, @Nullable Supplier<T> defSupplier)
	{
		myConsumers.add(properties ->
		{
			String value = StringUtil.nullize(String.valueOf(property.getValue()));
			if(value == null)
			{
				properties.remove(key);
				return;
			}
			properties.setProperty(key, value);
		});

		if(myConfigurationService.isNotInstalled())
		{
			if(defSupplier != null)
			{
				property.setValue(defSupplier.get());
			}
		}
		else
		{
			PropertySet propertySet = myConfigurationService.getPropertySet();

			String value = propertySet.getStringProperty(key);
			if(value != null)
			{
				if(clazz == Boolean.class)
				{
					property.setValue((T) Boolean.valueOf(value));
				}
				else if(clazz == String.class)
				{
					property.setValue((T) value);
				}
				else
				{
					throw new IllegalArgumentException(clazz.getName());
				}
			}
		}
	}

	@Nonnull
	private Component buildRepositoryGroup()
	{
		return buildGroup("Repository", layout ->
		{
			TextField workingDirectoryField = TinyComponents.newTextField();
			map(String.class, workingDirectoryField, PropertyKeys.WORKING_DIRECTORY, () -> SystemProperties.getUserHome() + File.separatorChar + ".consuloWebservice");

			layout.addComponent(VaadinUIUtil.labeledFill("Working directory: ", workingDirectoryField));

			TextField deployKeyField = TinyComponents.newTextField();
			map(String.class, deployKeyField, PropertyKeys.DEPLOY_KEY, null);

			layout.addComponent(VaadinUIUtil.labeledFill("Deploy key: ", deployKeyField));
		});
	}

	@Nonnull
	private Component buildCaptchaGroup()
	{
		return buildGroup("Captcha", layout ->
		{
			CheckBox enabledCaptcha = TinyComponents.newCheckBox("Enable captcha?");
			map(Boolean.class, enabledCaptcha, PropertyKeys.CAPTCHA_ENABLED_KEY, () -> false);
			layout.addComponent(enabledCaptcha);

			TextField privateApiKey = TinyComponents.newTextField();
			map(String.class, privateApiKey, PropertyKeys.CAPTCHA_PRIVATE_KEY, null);
			layout.addComponent(VaadinUIUtil.labeledFill("Private captcha key: ", privateApiKey));

			TextField siteApiKey = TinyComponents.newTextField();
			map(String.class, siteApiKey, PropertyKeys.CAPTCHA_SITE_KEY, null);
			layout.addComponent(VaadinUIUtil.labeledFill("Site captcha key: ", siteApiKey));

			privateApiKey.setEnabled(enabledCaptcha.getValue());
			siteApiKey.setEnabled(enabledCaptcha.getValue());

			enabledCaptcha.addValueChangeListener(event ->
			{
				privateApiKey.setEnabled(event.getValue());
				siteApiKey.setEnabled(event.getValue());
			});
		});
	}

	@Nonnull
	private Component buildGithubGroup()
	{
		return buildGroup("Github", layout ->
		{
			TextField oauthKeyField = TinyComponents.newTextField();
			map(String.class, oauthKeyField, GithubPropertyKeys.OAUTH_KEY, null);
			layout.addComponent(VaadinUIUtil.labeledFill("OAuth Key: ", oauthKeyField));

			TextField secretHookKeyField = TinyComponents.newTextField();
			map(String.class, secretHookKeyField, GithubPropertyKeys.SECRET_HOOK_KEY, null);
			layout.addComponent(VaadinUIUtil.labeledFill("Secret Hook Key: ", secretHookKeyField));
		});
	}

	@Nonnull
	private Component buildGAGroup()
	{
		return buildGroup("Google Analytics", layout ->
		{
			TextField trackerIdField = TinyComponents.newTextField();
			map(String.class, trackerIdField, GAPropertyKeys.TRACKER_ID, null);
			layout.addComponent(VaadinUIUtil.labeledFill("Tracker Id: ", trackerIdField));

			TextField domainNameField = TinyComponents.newTextField();
			map(String.class, domainNameField, GAPropertyKeys.DOMAIN_NAME, null);
			layout.addComponent(VaadinUIUtil.labeledFill("Domain Name: ", domainNameField));
		});
	}

	@Nonnull
	private static Panel buildGroup(String title, Consumer<VerticalLayout> consumer)
	{
		Panel panel = new Panel(title);
		panel.setSizeFull();

		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.setMargin(true);
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();
		verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

		panel.setContent(verticalLayout);

		consumer.accept(verticalLayout);
		return panel;
	}
}
