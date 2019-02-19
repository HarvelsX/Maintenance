/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018 KennyTV (https://github.com/KennyTV)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.kennytv.maintenance.api.event.proxy;

import eu.kennytv.maintenance.api.event.manager.MaintenanceEvent;

/**
 * Notification event fired when maintenance mode has been changed on a proxied server.
 *
 * @author KennyTV
 * @since 3.0.1
 */
public final class ServerMaintenanceChangedEvent implements MaintenanceEvent {
    private final String server;
    private final boolean maintenance;

    public ServerMaintenanceChangedEvent(final String server, final boolean maintenance) {
        this.server = server;
        this.maintenance = maintenance;
    }

    public String getServer() {
        return server;
    }

    public boolean isMaintenance() {
        return maintenance;
    }
}
