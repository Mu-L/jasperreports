package com.lowagie.text.pdf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Extension of {@link PdfStructureTreeRoot} that supports annotation
 * /StructParent entries in the ParentTree.
 *
 * For annotations with /StructParent (singular), the ParentTree value
 * must be a direct reference to the StructElem, not an array (which is
 * used for page /StructParents entries). This subclass overrides
 * {@link #buildTree()} to handle both types of entries correctly.
 */
public class PdfStructureTreeRootUtil extends PdfStructureTreeRoot
{

	private final Map<Integer, PdfIndirectReference> annotationParents = new HashMap<>();

	PdfStructureTreeRootUtil(PdfWriter writer)
	{
		super(writer);
	}

	/**
	 * Installs this extended tree root into the given PdfWriter,
	 * replacing the default PdfStructureTreeRoot.
	 * Must be called before any structure elements are created.
	 *
	 * @param writer the PdfWriter to install into
	 * @return the installed PdfStructureTreeRootUtil instance
	 */
	public static PdfStructureTreeRootUtil install(PdfWriter writer)
	{
		PdfStructureTreeRootUtil root = new PdfStructureTreeRootUtil(writer);
		writer.structureTreeRoot = root;
		return root;
	}

	/**
	 * Registers an annotation's StructParent entry.
	 *
	 * @param key the ParentTree key (to be set as /StructParent on the annotation)
	 * @param structElemReference the indirect reference to the StructElem
	 */
	public void addAnnotationParent(int key, PdfIndirectReference structElemReference)
	{
		annotationParents.put(key, structElemReference);
	}

	/**
	 * Returns the next available ParentTree key for annotation /StructParent values.
	 */
	public int nextParentTreeKey()
	{
		int maxKey = -1;

		try
		{
			java.lang.reflect.Field parentTreeField = PdfStructureTreeRoot.class.getDeclaredField("parentTree");
			parentTreeField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<Integer, PdfArray> parentTree = (Map<Integer, PdfArray>) parentTreeField.get(this);
			for (Integer key : parentTree.keySet())
			{
				if (key > maxKey)
				{
					maxKey = key;
				}
			}
		}
		catch (ReflectiveOperationException e)
		{
			throw new RuntimeException("Failed to access PdfStructureTreeRoot.parentTree", e);
		}

		for (Integer key : annotationParents.keySet())
		{
			if (key > maxKey)
			{
				maxKey = key;
			}
		}

		return maxKey + 1;
	}

	@Override
	void buildTree() throws IOException
	{
		PdfWriter writer = getWriter();

		Map<Integer, PdfArray> parentTree;
		try
		{
			java.lang.reflect.Field parentTreeField = PdfStructureTreeRoot.class.getDeclaredField("parentTree");
			parentTreeField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<Integer, PdfArray> pt = (Map<Integer, PdfArray>) parentTreeField.get(this);
			parentTree = pt;
		}
		catch (ReflectiveOperationException e)
		{
			throw new RuntimeException("Failed to access PdfStructureTreeRoot.parentTree", e);
		}

		Map<Integer, PdfIndirectReference> numTree = new HashMap<>();

		// Page-level entries: each value is a PdfArray of StructElem references
		for (Map.Entry<Integer, PdfArray> entry : parentTree.entrySet())
		{
			numTree.put(entry.getKey(), writer.addToBody(entry.getValue()).getIndirectReference());
		}

		// Annotation-level entries: each value is a direct StructElem reference
		numTree.putAll(annotationParents);

		PdfDictionary dicTree = PdfNumberTree.writeTree(numTree, writer);
		if (dicTree != null)
		{
			put(PdfName.PARENTTREE, writer.addToBody(dicTree).getIndirectReference());
		}

		nodeProcess(this, getReference(), writer);
	}

	private void nodeProcess(PdfDictionary dictionary, PdfIndirectReference reference, PdfWriter writer)
			throws IOException
	{
		PdfObject obj = dictionary.get(PdfName.K);
		if (obj != null && obj.isArray() && !((PdfArray) obj).getElements().isEmpty()
				&& !((PdfArray) obj).getElements().get(0).isNumber())
		{
			PdfArray ar = (PdfArray) obj;
			for (int k = 0; k < ar.size(); ++k)
			{
				PdfObject element = ar.getDirectObject(k);
				if (element instanceof PdfStructureElement)
				{
					PdfStructureElement e = (PdfStructureElement) element;
					ar.set(k, e.getReference());
					nodeProcess(e, e.getReference(), writer);
				}
			}
		}
		if (reference != null)
		{
			writer.addToBody(dictionary, reference);
		}
	}

}
