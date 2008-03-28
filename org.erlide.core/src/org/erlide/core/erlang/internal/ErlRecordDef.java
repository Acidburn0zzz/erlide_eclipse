package org.erlide.core.erlang.internal;

import org.erlide.core.erlang.IErlElement;
import org.erlide.core.erlang.IErlRecordDef;
import org.erlide.core.erlang.util.Util;

public class ErlRecordDef extends ErlMember implements IErlRecordDef {

	String record;

	/**
	 * @param parent
	 * @param imports
	 * @param module
	 */
	protected ErlRecordDef(IErlElement parent, String record) {
		super(parent, "record_definition");
		this.record = record;
	}

	public Kind getKind() {
		return Kind.RECORD_DEF;
	}

	public String getDefinedName() {
		return record;
	}

	@Override
	public String toString() {
		return getName() + ": " + getDefinedName();
	}

	@Override
	public int hashCode() {
		return Util.combineHashCodes(super.hashCode(), getDefinedName()
				.hashCode());
	}
}
