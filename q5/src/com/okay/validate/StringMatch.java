package com.okay.validate;

import java.util.ArrayList;

/**
 * @author Jason.Xiao
 * 
 */
public class StringMatch {
	public ArrayList OneList = new ArrayList();

	public ArrayList TwoList = new ArrayList();

	public int MatchRate = 0;

	private int[][] m_Matrix = null;


	public StringMatch() {
	}

	public void DoMatch() {
		int _rowCount = TwoList.size() + 1;
		int _colCount = OneList.size() + 1;
		m_Matrix = new int[_rowCount][_colCount];
		/** 初始化矩阵 */
		m_Matrix[0][0] = 0;
		for (int _col = 1; _col < _colCount; _col++) {
			m_Matrix[0][_col] = -_col - OneList.size();
		}
		for (int _row = 1; _row < _rowCount; _row++) {
			m_Matrix[_row][0] = -_row - OneList.size();
		}
		/** 匹配OneList & TwoList ,构造匹配矩阵 */
		int _totalValue = 0;
		for (int _col = 1; _col < _colCount; _col++) {
			_totalValue += _col;
			for (int _row = 1; _row < _rowCount; _row++) {
				int _dValue = OneList.size() + 1 - _col;
				if (!OneList.get(_col - 1).equals(TwoList.get(_row - 1))) {
					_dValue = -_dValue;
				}
				int _value1 = m_Matrix[_row - 1][_col - 1] + _dValue;
				int _value2 = m_Matrix[_row][_col - 1] - 1;
				int _value3 = m_Matrix[_row - 1][_col] - 1;
				int _value = _value1;
				_value = _value2 > _value ? _value2 : _value;
				_value = _value3 > _value ? _value3 : _value;
				m_Matrix[_row][_col] = _value;
			}
		}
		/** 得到矩阵的det系数 */
		int _cellValue = m_Matrix[_rowCount - 1][_colCount - 1];
		/** 计算匹配比率(除以n!即可) */
		MatchRate = (int) Math.round(100 * _cellValue / _totalValue);
	}

	public void Clear() {
		OneList.clear();
		TwoList.clear();
	}
	
	public void setOne(String _value){
		OneList.clear();
		for(int i=0;i<_value.length();i++){
			String _char=String.valueOf(_value.charAt(i));
			OneList.add(_char);
		}
	}

	public void setTwo(String _value){
		TwoList.clear();
		for(int i=0;i<_value.length();i++){
			String _char=String.valueOf(_value.charAt(i));
			TwoList.add(_char);
		}
	}

	public void PrintMatrix() {
		for (int i = 0; i <= TwoList.size(); i++) {
			String _line = "";
			for (int j = 0; j <= OneList.size(); j++) {
				_line += String.valueOf(m_Matrix[i][j]) + " ";
			}
		}
	}
	
	public static void main(String[] args) {
		StringMatch _match=new StringMatch();
//		_match.setOne("青岛地矿宾馆，宾馆现有客房 110 间，同时宾馆还...");
//		_match.setTwo("青岛地矿宾馆，（国土资源部青岛培训中心） , 酒店是三星级， 位于青岛市市南区 ,( 属于五四广场区域 ), 宾馆现有客房 110 间，同时宾馆还...");
		_match.setOne("青岛地矿宾馆，宾馆现有客房 110 间，同时宾馆还...");
		_match.setTwo("");
		_match.DoMatch();
		System.out.println(_match.MatchRate);
	}

}
