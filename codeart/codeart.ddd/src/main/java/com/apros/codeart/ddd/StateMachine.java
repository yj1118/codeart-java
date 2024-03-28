package com.apros.codeart.ddd;

import java.util.HashMap;
import java.util.Map;

final class StateMachine {

//	#region 状态

	private int _status = Status.Dirty.getValue() | Status.New.getValue() | Status.Changed.getValue();

	/**
	 * 
	 * 是否为脏对象
	 * 
	 * @param value
	 * @return
	 */
	public boolean isDirty() {
		return (_status & Status.Dirty.getValue()) == Status.Dirty.getValue();
	}

	private boolean isDirty(boolean value) {
		_status &= ~Status.Dirty.getValue();
		if (value)
			_status |= Status.Dirty.getValue();
	}

	public boolean isNew() {
		return (_status & Status.New.getValue()) == Status.New.getValue();
	}

	/**
	 * 内存中新创建的对象
	 * 
	 * @param value
	 * @return
	 */
	private boolean isNew(boolean value) {
		_status &= ~Status.New.getValue();
		if (value)
			_status |= Status.New.getValue();
	}

	public boolean isChanged() {
		return (_status & Status.Changed.getValue()) == Status.Changed.getValue();
	}

	/**
	 * 对象是否被改变
	 * 
	 * @param value
	 * @return
	 */
	private boolean isChanged(boolean value) {
		_status &= ~Status.Changed.getValue();
		if (value)
			_status |= Status.Changed.getValue();
	}

	public void markDirty() {
		this.isDirty(true);
	}

	public void MarkNew() {
		this.isNew(true);
	}

	public void markChanged() {
		this.isChanged(true);
	}

	/**
	 * 设置对象为干净的
	 */
	public void markClean() {
		this.isDirty(false);
		this.isNew(false);
		this.isChanged(false);
		_propertyChangedRecord.clear();
	}

	private Map<String, Boolean> _propertyChangedRecord = new HashMap<String, Boolean>();

	public void setPropertyChanged(String propertyName) {
		_propertyChangedRecord.put(propertyName, true);
		markDirty();
		markChanged();
	}

	public void clearPropertyChanged(String propertyName) {
		if (_propertyChangedRecord.remove(propertyName) != null) {
			if (_propertyChangedRecord.size() == 0) {
				this.isDirty(false);
				this.isChanged(false);
			}
		}

	}

	/**
	 * 判断属性是否被更改
	 * 
	 * @param propertyName
	 * @return
	 */
	public boolean isPropertyChanged(String propertyName) {
		// 如果是一个内存中新建的对象或者属性确实被改变了，我们认为属性被改变
		return _propertyChangedRecord.containsKey(propertyName);
	}

	/// <summary>
	/// 仅仅只是属性<paramref name="propertyName"/>发生了改变
	/// </summary>
	/// <param name="propertyName"></param>
	/// <returns></returns>
	public bool OnlyPropertyChanged(String propertyName) {
		return _propertyChangedRecord.Count == 1 && this.IsPropertyChanged(propertyName);
	}

	#endregion

	public StateMachine() {
	}

	private StateMachine(Status status, Dictionary<string, bool> changedRecord) {
		_status = status;
		_propertyChangedRecord = changedRecord;
	}

	public StateMachine Clone() {
		return new StateMachine(_status, new Dictionary<string, bool>(_propertyChangedRecord));
	}

	/// <summary>
	/// 合并状态，将目标已更改的属性更新到自身数据中
	/// </summary>
	/// <param name="target"></param>
	public void Combine(StateMachine target)
	{
	    var record = target._propertyChangedRecord;
	    foreach (var p in record)
	    {
	        var propertyName = p.Key;
	        if (!this.IsPropertyChanged(propertyName))
	            this.SetPropertyChanged(p.Key);
	    }
	}

	private static enum Status {

		/**
		 * 对象是否为脏的
		 */
		Dirty((byte) 0x1),
		/**
		 * 对象是否为新建的
		 */
		New((byte) 0x2),
		/**
		 * 对象是否被改变
		 */
		Changed((byte) 0x4);

		private final byte value;

		Status(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return this.value;
		}

	}

}
