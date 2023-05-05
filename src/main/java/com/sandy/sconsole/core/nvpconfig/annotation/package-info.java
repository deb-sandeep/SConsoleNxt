/**
 * This package contains convenience annotations for working with NVPConfig
 * entities.
 * <p/>
 * These annotations work only on Spring beans registered with the
 * ApplicationContext.
 * <p/>
 * A spring bean can have multiple fields marked with
 * {@link com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig} annotation.
 * These fields are populated with the persisted NVPConfig value in the
 * early phases of the SConsole bootrstrap. By default, the value of these
 * fields are updated on any change to the NVPConfig, however this behavior
 * can be turned off by the updateOnChange annotation attribute.
 * <p/>
 * {@link com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigGroup}
 * annotation can be used at a class level to provide a config group name
 * so that each NVPConfig need not specifiy the config group name. However,
 * if an NVPConfig specifies a config group name, it will take precedence
 * even if a class level config group name exists.
 */
package com.sandy.sconsole.core.nvpconfig.annotation;