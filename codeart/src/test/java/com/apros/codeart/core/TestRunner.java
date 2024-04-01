package com.apros.codeart.core;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.apros.codeart.App;

public class TestRunner implements BeforeAllCallback, AfterAllCallback {

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		App.initialize();
	}

	@Override
	public void afterAll(ExtensionContext arg0) throws Exception {
		App.dispose();
	}
}