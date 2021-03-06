package org.erlide.core.preferences;

import java.util.Arrays;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.erlide.engine.util.PreferencesHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

public class PreferencesHelperTest {

    private static final IScopeContext[] ALL_SCOPE_CONTEXTS = new IScopeContext[] {
            InstanceScope.INSTANCE, ConfigurationScope.INSTANCE, DefaultScope.INSTANCE };
    private static final String QUALIFIER = "org.erlide.testing";
    private static final String KEY = "key";

    @Before
    public void setUp() throws BackingStoreException {
        InstanceScope.INSTANCE.getNode(QUALIFIER).removeNode();
        ConfigurationScope.INSTANCE.getNode(QUALIFIER).removeNode();
        DefaultScope.INSTANCE.getNode(QUALIFIER).removeNode();
        Platform.getPreferencesService().getRootNode().flush();
    }

    @Test
    public void nextContexts_1() {
        final IScopeContext[] list = ALL_SCOPE_CONTEXTS;
        final IScopeContext item = ConfigurationScope.INSTANCE;
        final IScopeContext[] val = PreferencesHelper.getNextContexts(list, item);
        Assert.assertNotNull(val);
        Assert.assertEquals(1, val.length);
        Assert.assertEquals(DefaultScope.INSTANCE, val[0]);
    }

    @Test
    public void nextContexts_2() {
        final IScopeContext[] list = ALL_SCOPE_CONTEXTS;
        final IScopeContext item = DefaultScope.INSTANCE;
        final IScopeContext[] val = PreferencesHelper.getNextContexts(list, item);
        Assert.assertNotNull(val);
        Assert.assertEquals(0, val.length);
    }

    @Test
    public void nextContexts_3() {
        final IScopeContext[] list = ALL_SCOPE_CONTEXTS;
        final IScopeContext item = InstanceScope.INSTANCE;
        final IScopeContext[] val = PreferencesHelper.getNextContexts(list, item);
        Assert.assertNotNull(val);
        Assert.assertEquals(2, val.length);
        Assert.assertEquals(ConfigurationScope.INSTANCE, val[0]);
        Assert.assertEquals(DefaultScope.INSTANCE, val[1]);
    }

    @Test
    public void string_set() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        helper.putString(KEY, "smurf");
        final String res = helper.getString(KEY, "default");
        Assert.assertEquals("smurf", res);
    }

    @Test
    public void default_0() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        helper.putString(KEY, "balm");
        String res = InstanceScope.INSTANCE.getNode(QUALIFIER).get(KEY, null);
        Assert.assertNotNull(res);
        res = ConfigurationScope.INSTANCE.getNode(QUALIFIER).get(KEY, null);
        Assert.assertNull(res);
        res = DefaultScope.INSTANCE.getNode(QUALIFIER).get(KEY, null);
        Assert.assertNull(res);
    }

    @Test
    public void default_1() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        DefaultScope.INSTANCE.getNode(QUALIFIER).put(KEY, "balm");
        helper.putString(KEY, "balm");
        String res = InstanceScope.INSTANCE.getNode(QUALIFIER).get(KEY, null);
        Assert.assertNull(res);
        res = ConfigurationScope.INSTANCE.getNode(QUALIFIER).get(KEY, null);
        Assert.assertNull(res);
        res = DefaultScope.INSTANCE.getNode(QUALIFIER).get(KEY, null);
        Assert.assertNotNull(res);
    }

    @Test
    public void default_2() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        ConfigurationScope.INSTANCE.getNode(QUALIFIER).put(KEY, "balm");
        helper.putString(KEY, "balm");
        String res = InstanceScope.INSTANCE.getNode(QUALIFIER).get(KEY, null);
        Assert.assertNull(res);
        res = ConfigurationScope.INSTANCE.getNode(QUALIFIER).get(KEY, null);
        Assert.assertNotNull(res);
        res = DefaultScope.INSTANCE.getNode(QUALIFIER).get(KEY, null);
        Assert.assertNull(res);
    }

    @Test
    public void default_3() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        ConfigurationScope.INSTANCE.getNode(QUALIFIER).put(KEY, "balm");
        helper.putString(KEY, "smurf");
        String res = InstanceScope.INSTANCE.getNode(QUALIFIER).get(KEY, null);
        Assert.assertNotNull(res);
        res = ConfigurationScope.INSTANCE.getNode(QUALIFIER).get(KEY, null);
        Assert.assertNotNull(res);
        res = DefaultScope.INSTANCE.getNode(QUALIFIER).get(KEY, null);
        Assert.assertNull(res);
    }

    @Test
    public void byteArray_set() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        final byte[] val = new byte[] { 1, 3, 5, 7 };
        helper.putByteArray(KEY, val);
        final byte[] res = helper.getByteArray(KEY, null);
        Assert.assertEquals(Arrays.toString(val), Arrays.toString(res));
    }

    @Test
    public void double_set() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        final double val = 3.1415926d;
        helper.putDouble(KEY, val);
        final double res = helper.getDouble(KEY, Double.NaN);
        Assert.assertEquals(val, res, 1e-5);
    }

    @Test
    public void float_set() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        final float val = 3.1415926f;
        helper.putFloat(KEY, val);
        final float res = helper.getFloat(KEY, Float.NaN);
        Assert.assertEquals(val, res, 1e-5);
    }

    @Test
    public void long_set() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        final long val = 314159260;
        helper.putLong(KEY, val);
        final long res = helper.getLong(KEY, Long.MIN_VALUE);
        Assert.assertEquals(val, res);
    }

    @Test
    public void int_set() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        final int val = 314159;
        helper.putInt(KEY, val);
        final int res = helper.getInt(KEY, Integer.MIN_VALUE);
        Assert.assertEquals(val, res);
    }

    @Test
    public void removeLowestLevel() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        final String val = "value";
        final String defaultValue = "defdef";
        helper.putString(KEY, val);
        String res = helper.getString(KEY, defaultValue);
        Assert.assertEquals(val, res);

        helper.removeAllAtLowestScope();
        res = helper.getString(KEY, defaultValue);
        Assert.assertEquals(defaultValue, res);
    }

    @Test
    public void hasAnyLowestLevel_no1() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        final boolean has = helper.hasAnyAtLowestScope();
        Assert.assertEquals(false, has);
    }

    @Test
    public void hasAnyLowestLevel_yes() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        helper.putString(KEY, "gaga");
        final boolean has = helper.hasAnyAtLowestScope();
        Assert.assertEquals(true, has);
    }

    @Test
    public void hasAnyLowestLevel_no2() {
        final PreferencesHelper helper = PreferencesHelper.getHelper(QUALIFIER);
        helper.putString(KEY, "gaga");
        helper.removeAllAtLowestScope();
        final boolean has = helper.hasAnyAtLowestScope();
        Assert.assertEquals(false, has);
    }
}
