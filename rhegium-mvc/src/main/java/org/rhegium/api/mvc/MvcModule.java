package org.rhegium.api.mvc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourceprojects.cappadocia.ClassInfo;
import org.sourceprojects.cappadocia.ClassPool;
import org.sourceprojects.cappadocia.ClassQuery;
import org.sourceprojects.cappadocia.builder.ClassAttributeConstraintSelector;
import org.sourceprojects.cappadocia.builder.ClassQueryBuilder;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;


public class MvcModule extends AbstractModule {

	private static final Logger LOG = LoggerFactory.getLogger(MvcModule.class);
	private static Collection<Class<? extends ComponentController<?, ?>>> COMPONENT_CONTROLLERS = new ArrayList<Class<? extends ComponentController<?, ?>>>();
	private static Collection<Class<? extends View<?, ?>>> VIEWS = new ArrayList<Class<? extends View<?, ?>>>();

	static {
		reloadClassPool();
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected void configure() {
		Multibinder<ComponentController> binder = Multibinder.newSetBinder(binder(), ComponentController.class);

		for (Class<? extends ComponentController<?, ?>> componentController : COMPONENT_CONTROLLERS) {
			binder.addBinding().to(componentController).in(Singleton.class);
		}

		for (Class<? extends View<?, ?>> view : VIEWS) {
			bind(view);
		}
	}

	@SuppressWarnings("unchecked")
	private static Collection<Class<? extends ComponentController<?, ?>>> findComponentControllers(
			final ClassPool classPool) {

		LOG.info("Searching available ComponentControllers...");
		ClassQuery query = new ClassQueryBuilder() {

			@Override
			protected ClassAttributeConstraintSelector configure() {
				return where().subclassOf(AbstractComponentController.class.getCanonicalName());
			}
		}.build();

		if (classPool == null) {
			return Collections.emptyList();
		}

		Collection<ClassInfo> classInfos = query.list(classPool);
		Collection<Class<? extends ComponentController<?, ?>>> classes = new ArrayList<Class<? extends ComponentController<?, ?>>>();
		for (ClassInfo classInfo : classInfos) {
			try {
				LOG.info("\tFound ComponentController " + classInfo.getCanonicalName());
				classes.add((Class<? extends ComponentController<?, ?>>) Class.forName(classInfo.getCanonicalName()));
			}
			catch (Exception e) {
				LOG.warn("Class " + classInfo.getCanonicalName() + " not found");
			}
		}

		return classes;
	}

	@SuppressWarnings("unchecked")
	private static Collection<Class<? extends View<?, ?>>> findViews(final ClassPool classPool) {
		LOG.info("Searching available Views...");
		ClassQuery query = new ClassQueryBuilder() {

			@Override
			protected ClassAttributeConstraintSelector configure() {
				return where().subclassOf(AbstractComponentController.class.getCanonicalName());
			}
		}.build();

		if (classPool == null) {
			return Collections.emptyList();
		}

		Collection<ClassInfo> classInfos = query.list(classPool);
		Collection<Class<? extends View<?, ?>>> classes = new ArrayList<Class<? extends View<?, ?>>>();
		for (ClassInfo classInfo : classInfos) {
			try {
				LOG.info("\tFound View " + classInfo.getCanonicalName());
				classes.add((Class<? extends View<?, ?>>) Class.forName(classInfo.getCanonicalName()));
			}
			catch (Exception e) {
				LOG.warn("Class " + classInfo.getCanonicalName() + " not found");
			}
		}

		return classes;
	}

	/**
	 * Just to use prebuild of the collections.
	 */
	public static void load() {
	}

	public static void reloadClassPool() {
		try {
			ClassPool classPool = new ClassPool();
			classPool.addClasspath();

			COMPONENT_CONTROLLERS.clear();
			COMPONENT_CONTROLLERS.addAll(findComponentControllers(classPool));

			VIEWS.clear();
			VIEWS.addAll(findViews(classPool));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}