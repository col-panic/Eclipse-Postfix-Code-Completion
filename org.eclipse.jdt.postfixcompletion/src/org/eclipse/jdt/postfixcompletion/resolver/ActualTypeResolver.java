package org.eclipse.jdt.postfixcompletion.resolver;

import java.util.List;

import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.JavaVariable;
import org.eclipse.jdt.internal.corext.template.java.TypeResolver;
import org.eclipse.jdt.internal.ui.text.template.contentassist.MultiVariable;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;

/**
 * This class is responsible for resolving a given type or the type of another variable to its
 * actual type.
 */
@SuppressWarnings("restriction")
public class ActualTypeResolver extends TypeResolver {

	@Override
	public void resolve(TemplateVariable variable, TemplateContext context) {
		List<String> params= variable.getVariableType().getParams();
		if (params.size() > 0 && context instanceof JavaContext) {
			String param = params.get(0);
			JavaContext jc = (JavaContext) context;
			TemplateVariable ref = jc.getTemplateVariable(param);
			MultiVariable mv = (MultiVariable) variable;
			
			if (ref instanceof JavaVariable) {
				// Reference is another variable
				JavaVariable refVar = (JavaVariable) ref;
				jc.addDependency(refVar, mv);
				
				param = refVar.getParamType();
				if (param != null && "".equals(param) == false) {
					if (param.endsWith("[]")) { // In case of List<Integer[]> we must not remove []
						// Variable is an array, i.e. String[] or List<String>[]
						// Actual type is supposed to be:
						// String[]							=> String
						// List<String>[]					=> List<String>
						// String[][]						=> String[]
						param = param.substring(0, param.length() - 2);
					} else if (param.endsWith(">")) { // Generic
						// Actual type of a generic is supposed to be:
						// List<Integer>					=> Integer
						// List<List<Integer>>				=> List<Integer>
						// List<Map<Integer,String>>		=> Map<Integer,String>
						// Map<Integer,String>>				=> Integer
						// Something<Integer,Float,String>	=> Integer
						param = param.substring(param.indexOf("<") + 1, param.lastIndexOf(">"));
						if (param.contains(",")) {
							param = param.substring(0, param.indexOf(","));
						}
					} else {
						// The given parameter is already an actual type 
					}
					
					String reference = jc.addImport(param); // TODO Check if addImport(..) works correctly in cases of param == List<Integer>
					mv.setValue(reference);
					mv.setUnambiguous(true);
		
					mv.setResolved(true);
					return;
				}
			}
		}
		super.resolve(variable, context);
	}
}