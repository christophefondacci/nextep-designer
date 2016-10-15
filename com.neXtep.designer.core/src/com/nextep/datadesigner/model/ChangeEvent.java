/*******************************************************************************
 * Copyright (c) 2011 neXtep Software and contributors.
 * All rights reserved.
 *
 * This file is part of neXtep designer.
 *
 * NeXtep designer is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 *
 * NeXtep designer is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.datadesigner.model;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public enum ChangeEvent {

	NAME_CHANGED,
	SHORTNAME_CHANGED,
	DESCRIPTION_CHANGED,
	// When model is updated via set... methods
	MODEL_CHANGED,
	PARENT_CHANGED,
	CONTAINER_CHANGED,
	UPDATES_LOCKED,
	UPDATES_UNLOCKED,
	COLUMN_ADDED,
	COLUMN_REMOVED,
	COLUMN_TYPE_CHANGED,
	COLUMN_LENGTH_CHANGED,
	COLUMN_PRECISION_CHANGED,
	COLUMN_DEFAULT_CHANGED,
	COLUMN_NOTNULL_CHANGED,
	COLUMN_PK_CHANGED,
	SET_COLUMNS,
	CONSTRAINT_ADDED,
	CONSTRAINT_REMOVED,
	FLAGGED_PRIMARY,
	DATASET_ADDED,
	DATASET_REMOVED,
	DATALINE_ADDED,
	DATALINE_REMOVED,
	DEBRANCH,
	CHECKOUT,
	CHECKIN,
	VERSIONABLE_ADDED,
	VERSIONABLE_REMOVED,
	POSITION_CHANGED,
	ITEM_ADDED,
	ITEM_REMOVED,
	REMOTE_CONSTRAINT_CHANGED,
	COLUMN_CHANGED,
	RELEASE_CHANGED,
	ALIAS_CHANGED,
	// Dialog events
	VALIDATE,
	// Selection
	SELECTION_CHANGED,
	// Target management
	CONNECTION_ADDED,
	CONNECTION_REMOVED,
	// Connection management
	LOGIN_CHANGED,
	PASSWORD_CHANGED,
	DATABASE_CHANGED,
	PORT_CHANGED,
	IP_CHANGED,
	TARGET_TYPE_CHANGED,
	DBVENDOR_CHANGED,
	INSTANCE_CHANGED,
	// Generation management
	SCRIPT_GENERATED,
	BUILD_ADDED,
	SYNCH_STATUS_CHANGED,
	// Sequence management
	START_CHANGED,
	MIN_CHANGED,
	MAX_CHANGED,
	INCREMENT_CHANGED,
	CACHED_CHANGED,
	CYCLE_CHANGED,
	ORDERED_CHANGED,
	CACHE_SIZE_CHANGED,
	// Index management
	INDEX_ADDED,
	INDEX_REMOVED,
	// Package management
	SOURCE_CHANGED,
	// Code management
	BREAKPOINT_ADDED,
	BREAKPOINT_REMOVED,
	// Implementation management
	TABLESPACE_CHANGED,
	// Customization management
	CUSTOM_1,
	CUSTOM_2,
	CUSTOM_3,
	CUSTOM_4,
	CUSTOM_5,
	CUSTOM_6,
	CUSTOM_8,
	CUSTOM_9,
	CUSTOM_10,
	CUSTOM_11,
	CUSTOM_12,
	CUSTOM_13,
	// Generic child additions (should be used by extensions to contribute)
	GENERIC_CHILD_ADDED,
	GENERIC_CHILD_REMOVED,
	// Workarea related events
	POST_VIEW_CHANGED,
	// Generic progress event
	PROGRESSED,
	PROGRESS_TASKNAME,
	// Trigger events
	TRIGGER_TIME_CHANGED,
	TRIGGER_ADDED,
	TRIGGER_REMOVED,
	TRIGGER_EVENTS_CHANGED,
	// Partitioning events
	PARTITION_REMOVED,
	PARTITION_ADDED,
	// Physicals
	PHYSICAL_ATTR_CHANGED,
	MAPPING_CHANGED,
	REFERENCE_CHANGED,
	INVALIDATED,
	SWITCH_LISTENERS;
}
