package snowflake.components.files.browser.local;

import snowflake.common.FileInfo;
import snowflake.common.FileType;
import snowflake.common.local.files.LocalFileSystem;
import snowflake.components.files.FileComponentHolder;
import snowflake.components.files.browser.FileBrowser;
import snowflake.components.files.browser.folderview.FolderView;
import snowflake.utils.PathUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalMenuHandler {
    private JMenuItem mOpen, mRename, mDelete, mNewFile, mNewFolder, mCopy, mPaste, mCut, mAddToFav;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private FileBrowser fileBrowser;
    private FolderView folderView;
    private LocalFileOperations fileOperations;
    private LocalFileBrowserView fileBrowserView;
    private FileComponentHolder holder;

    public LocalMenuHandler(FileBrowser fileBrowser, LocalFileBrowserView fileBrowserView, FileComponentHolder holder) {
        this.fileBrowser = fileBrowser;
        this.holder = holder;
        this.fileOperations = new LocalFileOperations();
        this.fileBrowserView = fileBrowserView;
    }


    public void initMenuHandler(FolderView folderView) {
        this.folderView = folderView;
        InputMap map = folderView.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap act = folderView.getActionMap();
        this.initMenuItems();
    }

    private void initMenuItems() {
        mOpen = new JMenuItem("Open in new tab");
        mOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openNewTab();
            }
        });

        mRename = new JMenuItem("Rename");
        mRename.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rename(folderView.getSelectedFiles()[0], fileBrowserView.getCurrentDirectory());
            }
        });

        mDelete = new JMenuItem("Delete");
        mDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delete(folderView.getSelectedFiles());
            }
        });

        mNewFile = new JMenuItem("New file");
        mNewFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFile();
            }
        });

        mNewFolder = new JMenuItem("New folder");
        mNewFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFolder(fileBrowserView.getCurrentDirectory());
            }
        });

        mCopy = new JMenuItem("Copy");
        mCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //copyToClipboard(false);
            }
        });

        mPaste = new JMenuItem("Paste");
        mPaste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                if (AppClipboard.getContent() instanceof TransferFileInfo) {
//                    TransferFileInfo info = (TransferFileInfo) AppClipboard.getContent();
//                    localFolderView.pasteItem(info, folderView);
//                    if (info.getAction() == Action.CUT) {
//                        AppClipboard.setContent(null);
//                    }
//                }
            }
        });

        mCut = new JMenuItem("Cut");
        mCut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //copyToClipboard(true);
            }
        });

        mAddToFav = new JMenuItem("Bookmark");
        mAddToFav.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //addToFavourites();
            }
        });
    }


    public void createMenu(JPopupMenu popup, FileInfo[] selectedFiles) {
        createMenuContext(popup, selectedFiles);
    }

    private void createMenuContext(JPopupMenu popup, FileInfo[] files) {
        popup.removeAll();
        int selectionCount = files.length;
        createBuitinItems1(selectionCount, popup, files);
        createBuitinItems2(selectionCount, popup);
    }

    private void createBuitinItems1(int selectionCount, JPopupMenu popup, FileInfo[] selectedFiles) {
        if (selectedFiles[0].getType() == FileType.Directory
                || selectedFiles[0].getType() == FileType.DirLink) {
            popup.add(mOpen);
        }

        if (selectionCount == 1) {
            popup.add(mRename);
        }

//        if (selectionCount > 0) {
//            popup.add(mCopy);
//            popup.add(mCut);
//        }
    }

    private void createBuitinItems2(int selectionCount, JPopupMenu popup) {
        popup.add(mNewFolder);
        popup.add(mNewFile);
        // check only if folder is selected
        popup.add(mAddToFav);
    }

    private void openNewTab() {
        FileInfo files[] = folderView.getSelectedFiles();
        if (files.length == 1) {
            FileInfo file = files[0];
            if (file.getType() == FileType.Directory || file.getType() == FileType.DirLink) {
                fileBrowser.openLocalFileBrowserView(file.getPath(), this.fileBrowserView.getOrientation());
            }
        }
    }

    private void rename(FileInfo info, String baseFolder) {
        String text = JOptionPane
                .showInputDialog("Please enter new name", info.getName());
        if (text != null && text.length() > 0) {
            renameAsync(info.getPath(), PathUtils.combineUnix(PathUtils.getParent(info.getPath()), text), baseFolder);
        }
    }

    private void renameAsync(String oldName, String newName, String baseFolder) {
        executor.submit(() -> {
            fileBrowser.disableUi();
            if (fileOperations.rename(oldName, newName)) {
                fileBrowserView.render(baseFolder);
            } else {
                fileBrowser.enableUi();
            }
        });
    }

    private void delete(FileInfo[] selectedFiles) {
        executor.submit(() -> {
            fileBrowser.disableUi();
            for (FileInfo f : selectedFiles) {
                try {
                    new LocalFileSystem().delete(f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            fileBrowser.enableUi();
        });
    }

    private void newFile() {
        executor.submit(() -> {
            fileBrowser.disableUi();
            String baseFolder = fileBrowserView.getCurrentDirectory();
            if (fileOperations.newFile(baseFolder)) {
                fileBrowserView.render(baseFolder);
            } else {
                fileBrowser.enableUi();
            }
        });
    }

    private void newFolder(String currentDirectory) {
        executor.submit(() -> {
            fileBrowser.disableUi();
            String baseFolder = currentDirectory;
            if (fileOperations.newFile(baseFolder)) {
                fileBrowserView.render(baseFolder);
            } else {
                fileBrowser.enableUi();
            }
        });
    }
}
