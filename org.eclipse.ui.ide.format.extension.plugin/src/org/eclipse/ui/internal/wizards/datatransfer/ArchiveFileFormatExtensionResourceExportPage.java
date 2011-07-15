/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardExportResourcesPage;
import org.eclipse.ui.ide.format.extension.plugin.ui.data.ArchiveFileExportOperation;
import org.eclipse.ui.ide.format.extension.plugin.ui.data.DataTransferMessages;
import org.eclipse.ui.ide.format.extension.plugin.ui.data.TarFileExporter;

/**
 *	Page 1 of the base resource export-to-archive Wizard.
 *
 *	@since 3.1
 */
public class ArchiveFileFormatExtensionResourceExportPage extends WizardExportResourcesPage implements Listener {

    // widgets
    protected Button compressContentsCheckbox;
    
    protected Button zipFormatButton;
    protected Button tarFormatButton;

    protected Button uncompressTarButton;
    protected Button gzipCompressButton;
    protected Button bzip2CompressButton;
    
    private Combo destinationNameField;

    private Button destinationBrowseButton;
    
    protected Button createDirectoryStructureButton;

    protected Button createSelectionOnlyButton;

    // dialog store id constants
    private final static String STORE_DESTINATION_NAMES_ID = "WizardZipFileResourceExportPage1.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$

    private final static String STORE_CREATE_STRUCTURE_ID = "WizardZipFileResourceExportPage1.STORE_CREATE_STRUCTURE_ID"; //$NON-NLS-1$

    private final static String STORE_COMPRESS_CONTENTS_ID = "WizardZipFileResourceExportPage1.STORE_COMPRESS_CONTENTS_ID"; //$NON-NLS-1$

    private final static String STORE_ZIP_FORMAT_ID = "WizardZipFileResourceExportPage1.STORE_ZIP_FORMAT_ID";	//$NON-NLS-1$
    
    private final static String STORE_UNCOMPRESS_TAR_FORMAT_ID = "WizardZipFileResourceExportPage1.STORE_UNCOMPRESS_TAR_FORMAT_ID";	//$NON-NLS-1$
    
    private final static String STORE_GZIP_FORMAT_ID = "WizardZipFileResourceExportPage1.STORE_GZIP_FORMAT_ID"; //$NON-NLS-1$
    
    private final static String STORE_BZIP2_FORMAT_ID = "WizardZipFileResourceExportPage1.STORE_BZIP2_FORMAT_ID"; //$NON-NLS-1$
    
    public static final String PREFIX = PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$
    
    //string constants taken frmo IDataTransferHelpContextIds
    public static final String ZIP_FILE_EXPORT_WIZARD_PAGE = PREFIX
            + "zip_file_export_wizard_page"; //$NON-NLS-1$
    
    public static final String FILE_SYSTEM_EXPORT_WIZARD_PAGE = PREFIX
            + "file_system_export_wizard_page"; //$NON-NLS-1$
    
    /**
     *	Create an instance of this class. 
     *
     *	@param name java.lang.String
     */
    protected ArchiveFileFormatExtensionResourceExportPage(String name,
            IStructuredSelection selection) {
        super(name, selection);
    }

    /**
     * Create an instance of this class
     * @param selection the selection
     */
    public ArchiveFileFormatExtensionResourceExportPage(IStructuredSelection selection) {
        this("zipFileExportPage1", selection); //$NON-NLS-1$
        setTitle(DataTransferMessages.ArchiveExport_exportTitle);
        setDescription(DataTransferMessages.ArchiveExport_description);
    }

    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {
        super.createControl(parent);
        giveFocusToDestination();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
                FILE_SYSTEM_EXPORT_WIZARD_PAGE);  
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
                ZIP_FILE_EXPORT_WIZARD_PAGE);
    }

    /**
     * Handle all events and enablements for widgets in this page
     * @param e Event
     */
    public void handleEvent(Event e) {
        Widget source = e.widget;

        if (source == destinationBrowseButton) {
			handleDestinationBrowseButtonPressed();
		}

        updatePageCompletion();
    }
    
    /**
     *	Create the export destination specification widgets
     *
     *	@param parent org.eclipse.swt.widgets.Composite
     */
    protected void createDestinationGroup(Composite parent) {

        Font font = parent.getFont();
        // destination specification group
        Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        destinationSelectionGroup.setLayout(layout);
        destinationSelectionGroup.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
        destinationSelectionGroup.setFont(font);

        Label destinationLabel = new Label(destinationSelectionGroup, SWT.NONE);
        destinationLabel.setText(getDestinationLabel());
        destinationLabel.setFont(font);

        // destination name entry field
        destinationNameField = new Combo(destinationSelectionGroup, SWT.SINGLE
                | SWT.BORDER);
        destinationNameField.addListener(SWT.Modify, this);
        destinationNameField.addListener(SWT.Selection, this);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        destinationNameField.setLayoutData(data);
        destinationNameField.setFont(font);

        // destination browse button
        destinationBrowseButton = new Button(destinationSelectionGroup,
                SWT.PUSH);
        destinationBrowseButton.setText(DataTransferMessages.DataTransfer_browse);
        destinationBrowseButton.addListener(SWT.Selection, this);
        destinationBrowseButton.setFont(font);
        setButtonLayoutData(destinationBrowseButton);

        new Label(parent, SWT.NONE); // vertical spacer
    }
    
    /**
     * Create the buttons for the group that determine if the entire or
     * selected directory structure should be created.
     * @param optionsGroup
     * @param font
     */
    protected void createDirectoryStructureOptions(Composite optionsGroup, Font font) {
        // create directory structure radios
        createDirectoryStructureButton = new Button(optionsGroup, SWT.RADIO
                | SWT.LEFT);
        createDirectoryStructureButton.setText(DataTransferMessages.FileExport_createDirectoryStructure);
        createDirectoryStructureButton.setSelection(false);
        createDirectoryStructureButton.setFont(font);

        // create directory structure radios
        createSelectionOnlyButton = new Button(optionsGroup, SWT.RADIO
                | SWT.LEFT);
        createSelectionOnlyButton.setText(DataTransferMessages.FileExport_createSelectedDirectories);
        createSelectionOnlyButton.setSelection(true);
        createSelectionOnlyButton.setFont(font);
    }
    
    /**
     *	Create the export options specification widgets.
     *
     */
    protected void createOptionsGroupButtons(Group optionsGroup) {
    	Font font = optionsGroup.getFont();
    	optionsGroup.setLayout(new GridLayout(2, true));
    	
    	Composite left = new Composite(optionsGroup, SWT.NONE);
    	left.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    	left.setLayout(new GridLayout(1, true));

        createFileFormatOptions(left, font);

        Composite right = new Composite(optionsGroup, SWT.NONE);
        right.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
        right.setLayout(new GridLayout(1, true));

        createDirectoryStructureOptions(right, font);

        // initial setup
        createDirectoryStructureButton.setSelection(true);
        createSelectionOnlyButton.setSelection(false);
        compressContentsCheckbox.setSelection(true);

    }

    /**
     *	Set the contents of the receivers destination specification widget to
     *	the passed value
     *
     */
    protected void setDestinationValue(String value) {
        destinationNameField.setText(value);
    }
    
    /**
     * Create the buttons for the group that determine if the entire or
     * selected directory structure should be created.
     * @param optionsGroup
     * @param font
     */
    protected void createFileFormatOptions(Composite optionsGroup, Font font) {
        // create directory structure radios
        zipFormatButton = new Button(optionsGroup, SWT.RADIO | SWT.LEFT);
        zipFormatButton.setText(DataTransferMessages.ArchiveExport_saveInZipFormat);
        zipFormatButton.setSelection(true);
        zipFormatButton.setFont(font);
		zipFormatButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button)e.widget).getSelection()) {
					// activate Zip options
					activateZipOptions();
					setZipOption();
					// try setting the correct file extension
					setDestinationValue(getDestinationValue(true));
				}
			}
		});
        //create an indent
        Button tmp= new Button(optionsGroup, SWT.CHECK);
        int indent = tmp.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        tmp.dispose();
        // compress... checkbox
        compressContentsCheckbox = new Button(optionsGroup, SWT.CHECK
                | SWT.LEFT);
        compressContentsCheckbox.setText(DataTransferMessages.ZipExport_compressContents);
        compressContentsCheckbox.setFont(font);      
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		gridData.horizontalIndent = indent;
        compressContentsCheckbox.setLayoutData(gridData);

        // create directory structure radios
        tarFormatButton = new Button(optionsGroup, SWT.RADIO | SWT.LEFT);
        tarFormatButton.setText(DataTransferMessages.ArchiveExport_saveInTarFormat);
        tarFormatButton.setSelection(false);
        tarFormatButton.setFont(font);
		tarFormatButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(((Button)e.widget).getSelection()) {
					// activate tar options
					activateTarOptions();
					setUncompressedTarOption();
					// try setting the correct file extension
					setDestinationValue(getDestinationValue(true));
				}
			}
		});
        
        
        //uncompressed tar format
        uncompressTarButton = new Button(optionsGroup, SWT.RADIO | SWT.LEFT);
        uncompressTarButton.setText(DataTransferMessages.WizardArchiveFileResourceExportPage_UncompressedTarFormat);
        uncompressTarButton.setSelection(false);
        uncompressTarButton.setFont(font);
        uncompressTarButton.setLayoutData(gridData);
        uncompressTarButton.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		if(((Button)e.widget).getSelection()) {
        			//set uncompressed option
        			setUncompressedTarOption();
        			// try setting the correct file extension
        			setDestinationValue(getDestinationValue(true));
        		}
        	}
        });
        //gzip format tar ball
        gzipCompressButton = new Button(optionsGroup, SWT.RADIO | SWT.LEFT);
        gzipCompressButton.setText(DataTransferMessages.WizardArchiveFileResourceExportPage_GzipCompressedTarFormat);
        gzipCompressButton.setSelection(false);
        gzipCompressButton.setFont(font);
        gzipCompressButton.setLayoutData(gridData);
        gzipCompressButton.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		if(((Button)e.widget).getSelection()) {
        			//set gzip compress option
        			setGzipTarOption();
        			// try setting the correct file extension
        			setDestinationValue(getDestinationValue(true));
        		}
        	}
        });
        
        //bzip2 format tar ball
        bzip2CompressButton = new Button(optionsGroup, SWT.RADIO | SWT.LEFT);
        bzip2CompressButton.setText(DataTransferMessages.WizardArchiveFileResourceExportPage_Bzip2CompressedTarFormat);
        bzip2CompressButton.setSelection(false);
        bzip2CompressButton.setFont(font);
        bzip2CompressButton.setLayoutData(gridData);
        bzip2CompressButton.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		if(((Button)e.getSource()).getSelection()) {
        			//set bzip2 compress option
        			setBzipTarOption();
        			// try setting the correct file extension
        			setDestinationValue(getDestinationValue(true));
        		}
        	}
        });
    }    
    
    /**
     *	Set the current input focus to self's destination entry field
     */
    protected void giveFocusToDestination() {
        destinationNameField.setFocus();
    }
    
    /**
     * Attempts to ensure that the specified directory exists on the local file system.
     * Answers a boolean indicating success.
     *
     * @return boolean
     * @param directory java.io.File
     */
    protected boolean ensureDirectoryExists(File directory) {
        if (!directory.exists()) {
            if (!queryYesNoQuestion(DataTransferMessages.DataTransfer_createTargetDirectory)) {
				return false;
			}

            if (!directory.mkdirs()) {
                displayErrorDialog(DataTransferMessages.DataTransfer_directoryCreationError);
                giveFocusToDestination();
                return false;
            }
        }

        return true;
    }
    
    /**
     *	If the target for export does not exist then attempt to create it.
     *	Answer a boolean indicating whether the target exists (ie.- if it
     *	either pre-existed or this method was able to create it)
     *
     *	@return boolean
     */
    protected boolean ensureTargetIsValid(File targetDirectory) {
        if (targetDirectory.exists() && !targetDirectory.isDirectory()) {
            displayErrorDialog(DataTransferMessages.FileExport_directoryExists);
            giveFocusToDestination();
            return false;
        }

        return ensureDirectoryExists(targetDirectory);
    }
    
    /**
     * Returns a boolean indicating whether the directory portion of the
     * passed pathname is valid and available for use.
     */
    protected boolean ensureTargetDirectoryIsValid(String fullPathname) {
        int separatorIndex = fullPathname.lastIndexOf(File.separator);

        if (separatorIndex == -1) {
			return true;
		}

        return ensureTargetIsValid(new File(fullPathname.substring(0,
                separatorIndex)));
    }

    /**
     * Returns a boolean indicating whether the passed File handle is
     * is valid and available for use.
     */
    protected boolean ensureTargetFileIsValid(File targetFile) {
        if (targetFile.exists() && targetFile.isDirectory()) {
            displayErrorDialog(DataTransferMessages.ZipExport_mustBeFile);
            giveFocusToDestination();
            return false;
        }

        if (targetFile.exists()) {
            if (targetFile.canWrite()) {
                if (!queryYesNoQuestion(DataTransferMessages.ZipExport_alreadyExists)) {
					return false;
				}
            } else {
                displayErrorDialog(DataTransferMessages.ZipExport_alreadyExistsError);
                giveFocusToDestination();
                return false;
            }
        }

        return true;
    }

    /**
     * Ensures that the target output file and its containing directory are
     * both valid and able to be used.  Answer a boolean indicating validity.
     */
    protected boolean ensureTargetIsValid() {
        String targetPath = getDestinationValue(false);

        if (!ensureTargetDirectoryIsValid(targetPath)) {
			return false;
		}

        if (!ensureTargetFileIsValid(new File(targetPath))) {
			return false;
		}

        return true;
    }

    /**
     *  Export the passed resource and recursively export all of its child resources
     *  (iff it's a container).  Answer a boolean indicating success.
     */
    protected boolean executeExportOperation(ArchiveFileExportOperation op) {
        op.setCreateLeadupStructure(createDirectoryStructureButton
                .getSelection());
        op.setUseCompression(compressContentsCheckbox.getSelection());
        op.setUseTarFormat(tarFormatButton.getSelection());

        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            displayErrorDialog(e.getTargetException());
            return false;
        }

        IStatus status = op.getStatus();
        if (!status.isOK()) {
            ErrorDialog.openError(getContainer().getShell(),
                    DataTransferMessages.DataTransfer_exportProblems,
                    null, // no special message
                    status);
            return false;
        }

        return true;
    }

    /**
     * The Finish button was pressed.  Try to do the required work now and answer
     * a boolean indicating success.  If false is returned then the wizard will
     * not close.
     * @returns boolean
     */
    @SuppressWarnings("rawtypes")
	public boolean finish() {
    	List resourcesToExport = getWhiteCheckedResources();
    	
        if (!ensureTargetIsValid()) {
			return false;
		}

        //Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();
        // about to invoke the operation so save our state
        saveWidgetValues();

		// determine save format, if zip, use old ArchiveFileExportOperation
		// constructor
		// otherwise, tar format and call new ArchiveFileExportOperation
		// constructor with format code from TarFileExporter
        int tarCode = getTarExportCode();
        ArchiveFileExportOperation operation = null;
        if(tarCode != -1) { //tar is selected
        	try {
        	operation = new ArchiveFileExportOperation(null,
                    resourcesToExport, getDestinationValue(true), tarCode);
        	}catch(IllegalArgumentException iae) {
        		//this should never happen
        	}
        }else { //zip is selected
        	operation = new ArchiveFileExportOperation(null,
                    resourcesToExport, getDestinationValue(true));
        }
        return executeExportOperation(operation);
    }

    /**
     *	Answer the string to display in the receiver as the destination type
     */
    protected String getDestinationLabel() {
        return DataTransferMessages.ArchiveExport_destinationLabel;
    }

    /**
     *	Answer the contents of self's destination specification widget
     *
     *	@return java.lang.String
     */
    protected String getDestinationValue() {
        return destinationNameField.getText().trim();
    }
    
    /**
     *	Answer the contents of self's destination specification widget.  If this
     *	value does not have a suffix then add it first.
     *	@param radioActivated
     */
    protected String getDestinationValue(boolean radioActivated) {
    	String idealSuffix = getOutputSuffix();
        String destinationText = getDestinationValue();

        // only append a suffix if the destination doesn't already have a . in 
        // its last path segment.  
        // Also prevent the user from selecting a directory.  Allowing this will 
        // create a ".zip" file in the directory
        if (destinationText.length() != 0
                && !destinationText.endsWith(File.separator)) {
            int dotIndex = destinationText.lastIndexOf('.');
            if (dotIndex != -1) {
                //since the method is not called by radio button handler, we must determine the extension
            	if(!radioActivated)
            	{
            		int extIndex = getCompressionExtensionIndex(destinationText);
            		if(extIndex != -1)
            			idealSuffix = destinationText.substring(extIndex);
            	}
            	// the last path seperator index
                int pathSepIndex = destinationText.lastIndexOf(File.separator);
                if (pathSepIndex != -1 /*&& dotIndex < pathSepIndex*/) {
                    //detect if its one of the supported file extensions, if it is, replace the file extension
                	//otherwise, append
					int extIndex = getCompressionExtensionIndex(destinationText);
					if(extIndex != -1)
						destinationText = destinationText.substring(0, extIndex) + idealSuffix;
					else
						destinationText += idealSuffix;
                } 
            } else {
                destinationText += idealSuffix;
            }
        }

        return destinationText;
    }
    
    /**
     *  Returns the index of the beginning of the file extension, returns -1 if its not found.
     *  @param in
     *  @return int
     */
    protected int getCompressionExtensionIndex(String in)
    {
    	int index = -1;
    	if ((in.endsWith(".tar"))	 			//$NON-NLS-1$
				|| (in.endsWith(".zip"))		//$NON-NLS-1$
				|| (in.endsWith(".tgz"))		//$NON-NLS-1$
				|| (in.endsWith(".tbz"))		//$NON-NLS-1$
				|| (in.endsWith(".tbz2"))) {	//$NON-NLS-1$
    		index = in.lastIndexOf('.');
    	} else if(in.endsWith(".tar.gz")) {	//$NON-NLS-1$
			index = in.lastIndexOf(".tar.gz"); //$NON-NLS-1$
    	} else if(in.endsWith(".tar.bz2")) {	//$NON-NLS-1$
			index = in.lastIndexOf(".tar.bz2"); //$NON-NLS-1$
		}
    	return index;
    }

    /**
     *	Answer the suffix that files exported from this wizard should have.
     *	If this suffix is a file extension (which is typically the case)
     *	then it must include the leading period character.
     *
     */
	protected String getOutputSuffix() {
		if (zipFormatButton.getSelection()) {
			return ".zip"; //$NON-NLS-1$
		} else if (gzipCompressButton.getSelection()) {
			return ".tar.gz"; //$NON-NLS-1$
		} else if (bzip2CompressButton.getSelection()) {
			return ".tar.bz2"; //$NON-NLS-1$
		} else {
			return ".tar"; //$NON-NLS-1$
		}
	}

    /**
     *	Open an appropriate destination browser so that the user can specify a source
     *	to import from
     */
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE | SWT.SHEET);
        dialog.setFilterExtensions(new String[] { "*.zip;*.tar.gz;*.tar;*.tar.bz2", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setText(DataTransferMessages.ArchiveExport_selectDestinationTitle);
        String currentSourceString = getDestinationValue(false);
        int lastSeparatorIndex = currentSourceString
                .lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
			dialog.setFilterPath(currentSourceString.substring(0,
                    lastSeparatorIndex));
		}
        String selectedFileName = dialog.open();

        if (selectedFileName != null) {
            setErrorMessage(null);
            setDestinationValue(selectedFileName);
        }
    }

    /**
     *	Hook method for saving widget values for restoration by the next instance
     *	of this class.
     */
    protected void internalSaveWidgetValues() {
        // update directory names history
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings
                    .getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null) {
				directoryNames = new String[0];
			}

			directoryNames = addToHistory(directoryNames,
					getDestinationValue(false));
			settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);

			settings.put(STORE_CREATE_STRUCTURE_ID,
					createDirectoryStructureButton.getSelection());

			settings.put(STORE_COMPRESS_CONTENTS_ID,
					compressContentsCheckbox.getSelection());

			settings.put(STORE_ZIP_FORMAT_ID, zipFormatButton.getSelection());

			settings.put(STORE_UNCOMPRESS_TAR_FORMAT_ID,
					uncompressTarButton.getSelection());

			settings.put(STORE_GZIP_FORMAT_ID,
					gzipCompressButton.getSelection());

			settings.put(STORE_BZIP2_FORMAT_ID,
					bzip2CompressButton.getSelection());
             
        }
    }

    /**
     *	Add the passed value to self's destination widget's history
     *
     *	@param value java.lang.String
     */
    protected void addDestinationItem(String value) {
        destinationNameField.add(value);
    }
    
    /**
     *	Hook method for restoring widget values to the values that they held
     *	last time this wizard was used to completion.
     */
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings
                    .getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null || directoryNames.length == 0) {
				return; // ie.- no settings stored
			}

            // destination
            setDestinationValue(directoryNames[0]);
            for (int i = 0; i < directoryNames.length; i++) {
				addDestinationItem(directoryNames[i]);
			}

            boolean setStructure = settings
                    .getBoolean(STORE_CREATE_STRUCTURE_ID);

            createDirectoryStructureButton.setSelection(setStructure);
            createSelectionOnlyButton.setSelection(!setStructure);

            
            
            boolean zipFormat = settings.getBoolean(STORE_ZIP_FORMAT_ID);
            
			if (zipFormat) {
				activateZipOptions();
				setZipOption();
				compressContentsCheckbox.setSelection(settings
						.getBoolean(STORE_COMPRESS_CONTENTS_ID));
			} else {
				activateTarOptions();
				uncompressTarButton.setSelection(settings
						.getBoolean(STORE_UNCOMPRESS_TAR_FORMAT_ID));
				gzipCompressButton.setSelection(settings
						.getBoolean(STORE_GZIP_FORMAT_ID));
				bzip2CompressButton.setSelection(settings
						.getBoolean(STORE_BZIP2_FORMAT_ID));
			}
        }
    }
    
    /**
     * Returns the name of a container with a location that encompasses targetDirectory.
     * Returns null if there is no conflict.
     * 
     * @param targetDirectory the path of the directory to check.
     * @return the conflicting container name or <code>null</code>
     */
    protected String getConflictingContainerNameFor(String targetDirectory) {

        IPath rootPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        IPath testPath = new Path(targetDirectory);
        // cannot export into workspace root
        if(testPath.equals(rootPath))
        	return rootPath.lastSegment();
        
        //Are they the same?
        if(testPath.matchingFirstSegments(rootPath) == rootPath.segmentCount()){
        	String firstSegment = testPath.removeFirstSegments(rootPath.segmentCount()).segment(0);
        	if(!Character.isLetterOrDigit(firstSegment.charAt(0)))
        		return firstSegment;
        }

        return null;

    }
    
    /**
	 * Returns the name of a {@link IProject} with a location that includes
	 * targetDirectory. Returns null if there is no such {@link IProject}.
	 * 
	 * @param targetDirectory
	 *            the path of the directory to check.
	 * @return the overlapping project name or <code>null</code>
	 */
    @SuppressWarnings("deprecation")
	private String getOverlappingProjectName(String targetDirectory){
    	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    	IPath testPath = new Path(targetDirectory);
    	IContainer[] containers = root.findContainersForLocation(testPath);
    	if(containers.length > 0){
    		return containers[0].getProject().getName();
    	}
    	return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.wizards.datatransfer.WizardFileSystemResourceExportPage1#destinationEmptyMessage()
     */
    protected String destinationEmptyMessage() {
        return DataTransferMessages.ArchiveExport_destinationEmpty;
    }
    
    /**
     *	Answer a boolean indicating whether the receivers destination specification
     *	widgets currently all contain valid values.
     */
    protected boolean validateDestinationGroup() {
    	String destinationValue = getDestinationValue(false);
    	if (destinationValue.endsWith(".tar")) { //$NON-NLS-1$
    		//compressContentsCheckbox.setSelection(false);
    		activateTarOptions();
    		setUncompressedTarOption();
    	} else if (destinationValue.endsWith(".tar.gz") //$NON-NLS-1$
				|| destinationValue.endsWith(".tgz")) { //$NON-NLS-1$
    		//compressContentsCheckbox.setSelection(true);
    		activateTarOptions();
    		setGzipTarOption();
    	} else if (destinationValue.endsWith(".zip")) { //$NON-NLS-1$
    		activateZipOptions();
    		setZipOption();
    	}else if((destinationValue.endsWith(".tar.bz2"))	//$NON-NLS-1$
    			|| (destinationValue.endsWith(".tbz"))		//$NON-NLS-1$
    			|| (destinationValue.endsWith(".tbz2"))){	//$NON-NLS-1$
    		activateTarOptions();
    		setBzipTarOption();
    	}
    	if (destinationValue.length() == 0) {
            setMessage(destinationEmptyMessage());
            return false;
        }
        String conflictingContainer = getConflictingContainerNameFor(destinationValue);
        if (conflictingContainer == null) {
			// no error message, but warning may exists
			String threatenedContainer = getOverlappingProjectName(destinationValue);
			if(threatenedContainer == null)
				setMessage(null);
			else
				setMessage(
					NLS.bind(DataTransferMessages.FileExport_damageWarning, threatenedContainer),
					WARNING);
			
		} else {
            setErrorMessage(NLS.bind(DataTransferMessages.FileExport_conflictingContainer, conflictingContainer));
            giveFocusToDestination();
            return false;
        }

        return true;
    }
    
	/**
	 * Configure the compression format radio buttons when uncompressed tar
	 * format is selected.
	 */
	protected void setUncompressedTarOption() {
		tarFormatButton.setSelection(true);
		uncompressTarButton.setSelection(true);
		gzipCompressButton.setSelection(false);
		bzip2CompressButton.setSelection(false);
		zipFormatButton.setSelection(false);
	}

	/**
	 * Configure the compression format radio buttons when Gzip compressed tar
	 * format is selected.
	 */
	protected void setGzipTarOption() {
		tarFormatButton.setSelection(true);
		uncompressTarButton.setSelection(false);
		gzipCompressButton.setSelection(true);
		bzip2CompressButton.setSelection(false);
		zipFormatButton.setSelection(false);
	}

	/**
	 * Configure the compression format radio buttons when Zip format is
	 * selected.
	 */
	protected void setZipOption() {
		tarFormatButton.setSelection(false);
		uncompressTarButton.setSelection(false);
		gzipCompressButton.setSelection(false);
		bzip2CompressButton.setSelection(false);
		zipFormatButton.setSelection(true);
	}

	/**
	 * Configure the compression format radio buttons when Bzip2 compressed tar
	 * format is selected.
	 */
	protected void setBzipTarOption() {
		tarFormatButton.setSelection(true);
		uncompressTarButton.setSelection(false);
		gzipCompressButton.setSelection(false);
		bzip2CompressButton.setSelection(true);
		zipFormatButton.setSelection(false);
	}
	
	/**
	 *  Activate the Tar compression format options.
	 */
	protected void activateTarOptions()
	{
		 zipFormatButton.setSelection(false);
		 compressContentsCheckbox.setEnabled(false);
		 tarFormatButton.setSelection(true);   
		 uncompressTarButton.setEnabled(true);
		 gzipCompressButton.setEnabled(true);
		 bzip2CompressButton.setEnabled(true);
		 uncompressTarButton.setSelection(true);
	}
	
	/**
	 *  Activate the Zip compression format options.
	 */
	protected void activateZipOptions()
	{
		 zipFormatButton.setSelection(true);
		 compressContentsCheckbox.setEnabled(true);
		 tarFormatButton.setSelection(false);   
		 uncompressTarButton.setEnabled(false);
		 gzipCompressButton.setEnabled(false);
		 bzip2CompressButton.setEnabled(false);
	}
	
	/**
	 * Returns the tar mode code to the caller, based on the radio buttons'
	 * status.
	 */
	protected int getTarExportCode() {
		int code = -1;
		if (tarFormatButton.getSelection()) {
			if (uncompressTarButton.getSelection())
				code = TarFileExporter.UNCOMPRESSED;
			else if (gzipCompressButton.getSelection())
				code = TarFileExporter.GZIP;
			else if (bzip2CompressButton.getSelection())
				code = TarFileExporter.BZIP2;
		}
		return code;
	}
}
