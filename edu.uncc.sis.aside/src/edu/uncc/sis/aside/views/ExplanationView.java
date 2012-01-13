package edu.uncc.sis.aside.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.part.ViewPart;

public class ExplanationView extends ViewPart{

	private StyledText widget;
	private String detailDescription;

	private static String ABSTRACT = "ABSTRACT";
	private static String EXPLANATION = "EXPLANATION";
	private static String REMEDIATION = "REMEDIATION RECOMMENDATION";

	/*
	 * public 0-argument constructor required by extension point
	 */

	public ExplanationView() {
		super();
		detailDescription = "This view displays the detailed information about a piece of vulnerable code.";
	}

	@Override
	public void createPartControl(Composite composite) {
		final Display display = composite.getDisplay();
		widget = new StyledText(composite, SWT.READ_ONLY|SWT.V_SCROLL);
		widget.setText(detailDescription);
		widget.setMargins(20, 20, 20, 20);
		widget.setJustify(true);
		widget.setWordWrap(true);
		widget.setMargins(20, 20, 20, 20);

		final FontData data = widget.getFont().getFontData()[0];
		Font font = new Font(display, data.getName(), data.getHeight(),
				data.getStyle());
		
		widget.setFont(font);
		widget.setForeground(display.getSystemColor(SWT.COLOR_DARK_MAGENTA));
		widget.addListener(SWT.Resize, new Listener() {
			Image oldImage = null;

			public void handleEvent(Event event) {
				Rectangle rect = widget.getClientArea();
				Image newImage = new Image(Display.getDefault(), 1, Math.max(1,
						rect.height));
				GC gc = new GC(newImage);
				gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
				gc.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
				gc.fillGradientRectangle(rect.x, rect.y, 1, rect.height, true);
				gc.dispose();
				widget.setBackgroundImage(newImage);
				if (oldImage != null)
					oldImage.dispose();
				oldImage = newImage;
			}
		});

		widget.addExtendedModifyListener(new ExtendedModifyListener() {

			@Override
			public void modifyText(ExtendedModifyEvent event) {
				widget.update();
				detailDescription = widget.getText();
				
				StyleRange style = new StyleRange();
				style.font = new Font(display, data.getName(), data.getHeight(),
						SWT.BOLD);
				StyleRange[] styles = { style };
				int index1 = detailDescription.indexOf(ABSTRACT);
				int index2 = detailDescription.indexOf(EXPLANATION);
				int index3 = detailDescription.indexOf(REMEDIATION);

				if (index1 != -1) {
					widget.setStyleRanges(0, 0,
							new int[] { index1, ABSTRACT.length() }, styles);
				}

				if (index2 != -1) {
					widget.setStyleRanges(0, 0,
							new int[] { index2, EXPLANATION.length() }, styles);
				}

				if (index3 != -1) {
					widget.setStyleRanges(0, 0,
							new int[] { index3, REMEDIATION.length() }, styles);
				}
			}

		});

	}

	@Override
	public void setFocus() {
		widget.setFocus();
	}

	public StyledText getWidget() {
		return widget;
	}

	public void setGuidance(String text) {
		this.detailDescription = text;
	}

}
