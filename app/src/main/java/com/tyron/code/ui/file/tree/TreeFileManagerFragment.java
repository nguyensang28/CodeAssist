package com.tyron.code.ui.file.tree;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tyron.code.ui.component.tree.TreeNode;
import com.tyron.code.ui.component.tree.TreeView;
import com.tyron.code.ui.file.action.FileActionManager;
import com.tyron.code.ui.file.tree.binder.TreeFileNodeViewBinder.TreeFileNodeListener;
import com.tyron.code.ui.file.tree.binder.TreeFileNodeViewFactory;
import com.tyron.code.ui.file.tree.model.TreeFile;
import com.tyron.code.ui.main.MainFragment;
import com.tyron.code.ui.main.MainViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TreeFileManagerFragment extends Fragment {

    public static TreeFileManagerFragment newInstance(File root) {
        TreeFileManagerFragment fragment = new TreeFileManagerFragment();
        Bundle args = new Bundle();
        args.putSerializable("rootFile", root);
        fragment.setArguments(args);
        return fragment;
    }

    private File mRootFile;
    private MainViewModel mMainViewModel;
    private FileActionManager mActionManager;
    private TreeView<TreeFile> treeView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRootFile = (File) requireArguments().getSerializable("rootFile");
        mMainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout root = new FrameLayout(requireContext());
        root.setLayoutParams(
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));

        treeView = new TreeView<>(
                requireContext(), TreeNode.root(getNodes())
        );

        root.addView(treeView.getView(), new FrameLayout.LayoutParams(-1, -1));
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mActionManager = new FileActionManager();

        treeView.setAdapter(new TreeFileNodeViewFactory(new TreeFileNodeListener() {
            @Override
            public void onNodeToggled(TreeNode<TreeFile> treeNode, boolean expanded) {
                if (treeNode.isLeaf()) {
                    openFile(treeNode.getContent().getFile());
                }
            }

            @Override
            public boolean onNodeLongClicked(View view, TreeNode<TreeFile> treeNode, boolean expanded) {
                PopupMenu popupMenu = new PopupMenu(requireContext(), view);
                addMenus(popupMenu, treeNode);
                popupMenu.show();

                return true;
            }
        }));

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Add menus to the current PopupMenu based on the current {@link TreeNode}
     *
     * @param popupMenu The PopupMenu to add to
     * @param node      The current TreeNode in the file tree
     */
    private void addMenus(PopupMenu popupMenu, TreeNode<TreeFile> node) {
        mActionManager.addMenus(popupMenu, node, this);
    }

    public TreeView<TreeFile> getTreeView() {
        return treeView;
    }

    public MainViewModel getMainViewModel() {
        return mMainViewModel;
    }


    /**
     * Sets the tree to be rooted at this file, calls refresh() after
     *
     * @param file root file of the tree
     */
    public void setRoot(File file) {
        mRootFile = file;
        refresh();
    }

    public void refresh() {
        if (treeView != null) {
            treeView.refreshTreeView(TreeNode.root(getNodes()));
        }
    }

    private List<TreeNode<TreeFile>> getNodes() {
        List<TreeNode<TreeFile>> nodes = new ArrayList<>();
        if (mRootFile == null) {
            return nodes;
        }

        TreeNode<TreeFile> root = new TreeNode<>(
                TreeFile.fromFile(mRootFile), 0
        );
        root.setExpanded(true);

        File[] children = mRootFile.listFiles();
        if (children != null) {
            for (File file : children) {
                addNode(root, file, 1);
            }
        }
        nodes.add(root);
        return nodes;
    }

    private void addNode(TreeNode<TreeFile> node, File file, int level) {
        TreeNode<TreeFile> childNode = new TreeNode<>(
                TreeFile.fromFile(file), level
        );

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addNode(childNode, child, level + 1);
                }
            }
        }

        node.addChild(childNode);
    }

    private void openFile(File file) {
        Fragment parent = getParentFragment();

        if (parent != null) {
            if (parent instanceof MainFragment) {
                ((MainFragment) parent).openFile(file);
            }
        }
    }
}

