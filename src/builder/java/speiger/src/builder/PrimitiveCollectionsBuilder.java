package speiger.src.builder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import speiger.src.builder.processor.TemplateProcess;
import speiger.src.builder.processor.TemplateProcessor;

@SuppressWarnings("javadoc")
public class PrimitiveCollectionsBuilder extends TemplateProcessor
{
	Map<String, EnumSet<ClassType>> blocked = new HashMap<>();
	Map<String, String> nameRemapper = new HashMap<>();
	Map<String, String> biRequired = new HashMap<>();
	Set<String> enumRequired = new HashSet<>();
	public static final ClassType[] TYPE = ClassType.values();
	List<GlobalVariables> variables = new ArrayList<>();
	List<GlobalVariables> biVariables = new ArrayList<>();
	List<GlobalVariables> enumVariables = new ArrayList<>();
	
	public PrimitiveCollectionsBuilder()
	{
		super(Paths.get("src/builder/resources/speiger/assets/collections/templates/"), Paths.get("src/main/java/speiger/src/collections/"), Paths.get("src/builder/resources/speiger/assets/collections/"));
	}
	
	public PrimitiveCollectionsBuilder(Path sourceFolder, Path outputFolder, Path dataFolder)
	{
		super(sourceFolder, outputFolder, dataFolder);
	}
	
	@Override
	protected boolean isFileValid(Path fileName)
	{
		return true;
	}
	
	@Override
	protected boolean relativePackages()
	{
		return true;
	}
	
	@Override
	protected boolean debugUnusedMappers()
	{
		return false;
	}
	
	@Override
	protected void init()
	{
		variables.clear();
		for(ClassType clzType : TYPE)
		{
			for(ClassType subType : TYPE)
			{
				create(clzType, subType);
			}
		}
		enumRequired.add("EnumMap");
		enumRequired.add("LinkedEnumMap");
		biRequired.put("BiConsumer", "");
		biRequired.put("UnaryOperator", "");
		biRequired.put("Pair", "");
		biRequired.put("MutablePair", "");
		biRequired.put("ImmutablePair", "");
		addBiClass("Function", "Maps", "Map", "SortedMap", "NavigableMap", "AbstractMap", "ImmutableOpenHashMap", "OpenHashMap", "LinkedOpenHashMap", "OpenCustomHashMap", "LinkedOpenCustomHashMap", "ArrayMap", "RBTreeMap", "AVLTreeMap");
		nameRemapper.put("BiConsumer", "%sConsumer");
		nameRemapper.put("IArray", "I%sArray");
		nameRemapper.put("AbstractMap", "Abstract%sMap");
		nameRemapper.put("AbstractCollection", "Abstract%sCollection");
		nameRemapper.put("AbstractPriorityQueue", "Abstract%sPriorityQueue");
		nameRemapper.put("AbstractSet", "Abstract%sSet");
		nameRemapper.put("AbstractList", "Abstract%sList");
		nameRemapper.put("EnumMap", "Enum2%sMap");
		nameRemapper.put("LinkedEnumMap", "LinkedEnum2%sMap");
		nameRemapper.put("ImmutableList", "Immutable%sList");
		nameRemapper.put("ImmutableOpenHashSet", "Immutable%sOpenHashSet");
		nameRemapper.put("ImmutableOpenHashMap", "Immutable%sOpenHashMap");
		
		addBlockage(ClassType.OBJECT, "Consumer", "Comparator", "Stack");
		addBlockage(ClassType.BOOLEAN, "ArraySet", "AVLTreeSet", "RBTreeSet", "SortedSet", "NavigableSet", "OpenHashSet", "OpenCustomHashSet", "LinkedOpenHashSet", "LinkedOpenCustomHashSet");
		addBlockage(ClassType.BOOLEAN, "ImmutableOpenHashMap", "ImmutableOpenHashSet", "SortedMap", "NavigableMap", "OpenHashMap", "LinkedOpenHashMap", "OpenCustomHashMap", "LinkedOpenCustomHashMap", "ArrayMap", "RBTreeMap", "AVLTreeMap");
	}
	
	protected void create(ClassType mainType, ClassType subType) 
	{
		GlobalVariables type = new GlobalVariables(mainType, subType);
		type.createFlags();
		type.createHelperVariables();
		type.createVariables();
		type.createPreFunctions();
		type.createClassTypes();
		type.createFunctions();
		if(mainType == subType) variables.add(type);
		biVariables.add(type);
		if(mainType.isObject()) enumVariables.add(type);
	}
	
	protected void addBiClass(String...classNames) 
	{
		for(String s : classNames)
		{
			biRequired.put(s, "2");
		}
	}
	
	protected void addBlockage(ClassType type, String...args) 
	{
		for(String s : args) 
		{
			EnumSet<ClassType> set = blocked.get(s);
			if(set == null) 
			{
				set = EnumSet.noneOf(ClassType.class);
				blocked.put(s, set);
			}
			set.add(type);
		}
	}
	
	@Override
	public void createProcesses(String name, Consumer<TemplateProcess> acceptor)
	{
		String splitter = biRequired.get(name);
		boolean valueRequired = enumRequired.contains(name);
		List<GlobalVariables> vars = getVariablesByClass(name, splitter != null);
		EnumSet<ClassType> types = blocked.get(name);
		for(int i = 0,m=vars.size();i<m;i++)
		{
			GlobalVariables type = vars.get(i);
			if(types == null || !types.contains(type.getType()))
			{
				acceptor.accept(type.create(nameRemapper.getOrDefault(name, "%s"+name), splitter, valueRequired));
			}
		}
	}
	
	protected List<GlobalVariables> getVariablesByClass(String name, boolean bi) {
		if(enumRequired.contains(name)) return enumVariables;
		if(bi) return biVariables;
		return variables;
	}
	
	public static void main(String...args)
	{
		try
		{
	        if(args.length == 0) {
	            new PrimitiveCollectionsBuilder().process(false);
	        } else if(args.length == 1) {
	            new PrimitiveCollectionsBuilder().process(Boolean.parseBoolean(args[0]));
	        } else if(args.length == 3) {
	            new PrimitiveCollectionsBuilder(Paths.get(args[0]), Paths.get(args[1]), Paths.get(args[2])).process(false);
	        } else if(args.length == 4) {
	            new PrimitiveCollectionsBuilder(Paths.get(args[0]), Paths.get(args[1]), Paths.get(args[2])).process(Boolean.parseBoolean(args[3]));
	        } else {
	            System.out.println("Invalid argument count passed in");
	            System.exit(1);
	    	}
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
