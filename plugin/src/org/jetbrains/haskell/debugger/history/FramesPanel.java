package org.jetbrains.haskell.debugger.history;

import com.intellij.ui.components.JBList;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class FramesPanel extends JBList {
  private DefaultListModel<String> listModel = new DefaultListModel<String>();

  public FramesPanel(final HistoryManager manager) {
    setModel(listModel);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setValueIsAdjusting(true);
    addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        manager.indexSelected(getSelectedIndex());
      }
    });
  }

  @Override
  public ListModel<String> getModel() {
    return listModel;
  }

  public void addElement(String line) {
    listModel.addElement(line);
    if (listModel.size() == 1) {
      setSelectedIndex(0);
    }
  }

  public void clear() {
    listModel.clear();
  }

  public int getIndexCount() {
    return listModel.size();
  }

  public boolean isFrameUnknown() {
    return getSelectedIndex() < 0 || listModel.get(getSelectedIndex()).equals("...");
  }
}
