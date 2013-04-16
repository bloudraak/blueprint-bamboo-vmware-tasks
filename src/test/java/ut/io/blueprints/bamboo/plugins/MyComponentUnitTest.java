package ut.io.blueprints.bamboo.plugins;

import org.junit.Test;
import io.blueprints.bamboo.plugins.MyPluginComponent;
import io.blueprints.bamboo.plugins.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}