package ai.preferred.cerebro.index.extra;

public class TestConst {
    public final static String DIM_50_PATH = "C:\\50_dims_testing\\";
    public final static String TXT_DATA_PATH = "E:\\Lucene\\imdb_data\\";
    public final static String DIM_100_PATH = "E:\\100_dims_testing\\";
    public final static String HNSW_PATH_SINGLE = "E:\\hnsw_single_segment\\";
    public final static String HNSW_PATH_MULTI = "E:\\hnsw_multi_segment\\";
    public static final String FilePathField = "path_to_file";


    public static String text1 = "The Last Command and City Lights, that latter Chaplin circa 1931";
    public static String text2 = "William Boyd and Louis Wolheim are the \"Two Arabian Knights\" referred to in the title";
    public static String text3 = "two WW I soldiers escape from a German prison camp";

    public static double[] vec1 = {0.38104349, 0.02754851, 0.05163061, 0.37526948};
    public static double[] vec2 = {0.74279698, 0.6226621 , 0.79427658, 0.15585331};
    public static double[] vec3 = {0.53802814, 0.89592585, 0.07040728, 0.28527442};

    public static double[][] hashingVecs = {{-0.10573825,  0.52355403,  0.99642232, -0.75576527},
            {-0.96525557, -0.66935669,  0.24867779,  0.37252496},
            {-0.20823929,  0.29177013, -0.07020453,  0.0677263 },
            { 0.59251946,  0.60730828,  0.21524159,  0.12454949},
            {0.08847298,  0.79887931, -0.71255336,  0.76523301},
            {0.77077078, -0.58742485, -0.00717082,  0.54887554},
            {-0.74913   ,  0.45409921, -0.25328846, -0.83232725},
            {-0.55407584, -0.31096339, -0.31309828, -0.25760011}};
}
