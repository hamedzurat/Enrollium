package enrollium.client.page.students;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import enrollium.client.Resources;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextFlow;

import java.util.List;
import java.util.Random;


public class OfferedCoursePage extends BasePage {
    public static final  TranslationKey NAME       = TranslationKey.COURSE;
    private static final String         IMAGE_PATH = "images/courses/";
    private static final Random         RANDOM     = new Random();

    public OfferedCoursePage() {
        super();

        List<OfferedCourseData> offeredCourses = List.of( //
                OfferedCourseData.builder()
                                 .imgFile("zach-graves-wtpTL_SzmhM-unsplash.jpg")
                                 .trimester(1)
                                 .courseCode("CSE1110")
                                 .titleEn("Introduction to Computer Systems")
                                 .titleBn("কম্পিউটার সিস্টেম পরিচিতি")
                                 .descriptionEn("The history of computing devices, and the major components of a computer, including hardware and software. It covers computer architecture, basic IT concepts, the Internet, and number systems like binary, octal, and hexadecimal. Programming fundamentals include development stages, flowcharts, data types, operators, control statements, functions, and arrays.")
                                 .descriptionBn("কম্পিউটিং ডিভাইসের ইতিহাস এবং হার্ডওয়্যার এবং সফ্টওয়্যার সহ একটি কম্পিউটারের প্রধান উপাদান। এটি কম্পিউটার আর্কিটেকচার, বেসিক আইটি ধারণা, ইন্টারনেট এবং বাইনারি, অক্টাল এবং হেক্সাডেসিমালের মতো সংখ্যা সিস্টেমগুলিকে কভার করে। প্রোগ্রামিং ফান্ডামেন্টালগুলির মধ্যে রয়েছে ডেভেলপমেন্ট স্টেজ, ফ্লোচার্ট, ডেটা টাইপ, অপারেটর, কন্ট্রোল স্টেটমেন্ট, ফাংশন এবং অ্যারে।")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("kenny-eliason-h0rXrHzhFXU-unsplash.jpg")
                                 .trimester(1)
                                 .courseCode("ENG1011")
                                 .titleEn("English – I")
                                 .titleBn("ইংরেজি – ১")
                                 .descriptionEn("The focus is on developing [b]reading, writing, speaking, [/b]and[b] listening [/b]skills through strategies like skimming, scanning, brainstorming, and note-taking. Key topics include grammatical knowledge, linking words, summarizing, creative writing, and presentation techniques. Speaking and listening emphasize pronunciation, intonation, vocabulary, impromptu speaking, group presentations, and engaging with drama, famous speeches, and listening activities[code][/code]")
                                 .descriptionBn("স্কিমিং, স্ক্যানিং, ব্রেইনস্টর্মিং এবং নোট নেওয়ার মতো কৌশলগুলির মাধ্যমে [বি] পড়া, লেখা, কথা বলা, [/খ] এবং [খ] শোনার দক্ষতা বিকাশের দিকে মনোনিবেশ করা হয়েছে। মূল বিষয়গুলির মধ্যে রয়েছে ব্যাকরণগত জ্ঞান, শব্দ সংযোগ, সংক্ষিপ্তকরণ, সৃজনশীল লেখা এবং উপস্থাপনা কৌশল। স্পিকিং এবং শ্রবণ উচ্চারণ, স্বরভঙ্গি, শব্দভাণ্ডার, তাত্ক্ষণিক বক্তৃতা, গ্রুপ উপস্থাপনা এবং নাটক, বিখ্যাত বক্তৃতা এবং শ্রবণ ক্রিয়াকলাপের সাথে জড়িত থাকার উপর জোর দেয়[কোড][/কোড]")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("aaron-lefler-Vs6ip7fsld8-unsplash.jpg")
                                 .trimester(1)
                                 .courseCode("CSE2213")
                                 .titleEn("Discrete Mathematics")
                                 .titleBn("বিচ্ছিন্ন গণিত")
                                 .descriptionEn("Set theory: sets, relations, functions; Mathematical Logic: propositional calculus and predicate calculus; Mathematical reasoning and proof techniques; Counting: permutations, combinations, Discrete Probability principles of inclusion and exclusion; Recurrence relations; Graph Theory: graphs, paths, and trees.")
                                 .descriptionBn("সেট তত্ত্ব: সেট, সম্পর্ক, ফাংশন; গাণিতিক যুক্তি: প্রস্তাবনামূলক ক্যালকুলাস এবং বিধেয় ক্যালকুলাস; গাণিতিক যুক্তি এবং প্রমাণ কৌশল; গণনা: পারমুটেশন, সংমিশ্রণ, অন্তর্ভুক্তি এবং বর্জনের বিচ্ছিন্ন সম্ভাব্যতা নীতি; পুনরাবৃত্তি সম্পর্ক; গ্রাফ থিওরি: গ্রাফ, পাথ এবং গাছ।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("austin-curtis-sk8LPCMvw_A-unsplash.jpg")
                                 .trimester(1)
                                 .courseCode("BDS1201")
                                 .titleEn("History of the Emergence of Bangladesh")
                                 .titleBn("বাংলাদেশের অভ্যুদয়ের ইতিহাস")
                                 .descriptionEn("The topics cover key historical events like the Partition of Bengal ([b]1947[/b]), the Language Movement ([b]1952[/b]), the Liberation War, and Bangladesh's emergence as an independent state in [b]1971[/b]. They also explore the Constitution and citizen rights, Bengali culture, social problems, and the impact of urbanization. Additionally, theories of social change and their implications for Bangladeshi society are discussed[code][/code]")
                                 .descriptionBn("১৯৪৭ সালের বঙ্গভঙ্গ ([খ]), ভাষা আন্দোলন (১৯৫২[/খ]), মুক্তিযুদ্ধ এবং ১৯৭১ সালে স্বাধীন রাষ্ট্র হিসেবে বাংলাদেশের অভ্যুদয়ের মতো গুরুত্বপূর্ণ ঐতিহাসিক ঘটনাবলী নিয়ে আলোচনা করা হয়েছে। তারা সংবিধান ও নাগরিক অধিকার, বাঙালি সংস্কৃতি, সামাজিক সমস্যা এবং নগরায়নের প্রভাব অন্বেষণ করে। উপরন্তু, সামাজিক পরিবর্তনের তত্ত্ব এবং বাংলাদেশী সমাজে তাদের প্রভাব নিয়ে আলোচনা করা হয়েছে[কোড][/কোড]")
                                 .type("Theory")
                                 .credits(2)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("jeswin-thomas-hecib2an4T4-unsplash.jpg")
                                 .trimester(2)
                                 .courseCode("MATH1151")
                                 .titleEn("Fundamental Calculus")
                                 .titleBn("মৌলিক ক্যালকুলাস")
                                 .descriptionEn("Function, Domain and Range of a Function. Translation and reflection of a function. Even and Odd functions, Inverse functions, One to One and many to one function. Limit, continuity and differentiability, Tangent line, Differentiation of different types of functions.  An overview of area problem,  Newton’s anti-derivative method  in finding area, Indefinite integral, fundamental theorem of calculus, Definite integral, Area between two curves. Different types of Integration (Principles of Integral evaluation, Integration by parts, Trigonometric Substitution).")
                                 .descriptionBn("ফাংশন, ডোমেন এবং একটি ফাংশন পরিসীমা। একটি ফাংশন অনুবাদ এবং প্রতিফলন। জোড় এবং বিজোড় ফাংশন, বিপরীত ফাংশন, এক থেকে এক এবং অনেক থেকে এক ফাংশন। সীমা, ধারাবাহিকতা এবং পার্থক্যযোগ্যতা, স্পর্শক লাইন, বিভিন্ন ধরণের ফাংশনের পার্থক্য।  অঞ্চল সমস্যার একটি ওভারভিউ, ক্ষেত্রফল সন্ধানে নিউটনের অ্যান্টি-ডেরিভেটিভ পদ্ধতি, ক্যালকুলাসের অনির্দিষ্ট সমাকলন, মৌলিক উপপাদ্য, নির্দিষ্ট সমাকলন, দুটি বক্ররেখার মধ্যে ক্ষেত্রফল। বিভিন্ন ধরণের ইন্টিগ্রেশন (ইন্টিগ্রাল মূল্যায়নের মূলনীতি, অংশ দ্বারা একীকরণ, ত্রিকোণমিতিক প্রতিস্থাপন)।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("product-school-cd7i9vYIyeY-unsplash.jpg")
                                 .trimester(2)
                                 .courseCode("ENG1013")
                                 .titleEn("English – II")
                                 .titleBn("ইংরেজি – ২")
                                 .descriptionEn("The focus is on improving writing skills through free and guided writing, paragraph and essay writing (descriptive, narrative, cause-effect, compare-contrast, and argumentative), editing for errors, and application/email writing. Vocabulary building includes academic word usage. Reading comprehension and listening practice are emphasized, along with speaking skills like public speaking and argumentative presentations.")
                                 .descriptionBn("বিনামূল্যে এবং নির্দেশিত লেখা, অনুচ্ছেদ এবং প্রবন্ধ লেখার (বর্ণনামূলক, আখ্যান, কারণ-প্রভাব, তুলনা-বৈসাদৃশ্য এবং যুক্তিযুক্ত), ত্রুটিগুলির জন্য সম্পাদনা এবং অ্যাপ্লিকেশন / ইমেল লেখার মাধ্যমে লেখার দক্ষতা উন্নত করার দিকে মনোনিবেশ করা হয়। ভোকাবুলারি বিল্ডিং একাডেমিক শব্দ ব্যবহার অন্তর্ভুক্ত। জনসাধারণের বক্তৃতা এবং যুক্তিপূর্ণ উপস্থাপনার মতো কথা বলার দক্ষতার পাশাপাশি পড়ার বোধগম্যতা এবং শ্রবণ অনুশীলনের উপর জোর দেওয়া হয়।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("ENG1011")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("uday-awal-UjJWhMerJx0-unsplash.jpg")
                                 .trimester(2)
                                 .courseCode("CSE1111")
                                 .titleEn("Structured Programming Language")
                                 .titleBn("স্ট্রাকচার্ড প্রোগ্রামিং ল্যাঙ্গুয়েজ")
                                 .descriptionEn("Basic understanding of problem solving; Structured programming language: data types, operators, expressions, control structures (If-else, Switch-case, Loop); Functions and program structure: parameter passing conventions, scope rules and storage classes, recursion; Header files; Pointers and arrays; Strings; Multidimensional array; User defined data types: structures, unions, enumerations; Input and Output: standard input and output, formatted input and output, file access; Variable length argument list; Command line parameters; Error Handling; Graphics; Linking; Library functions.")
                                 .descriptionBn("সমস্যা সমাধানের মৌলিক ধারণা; স্ট্রাকচার্ড প্রোগ্রামিং ল্যাঙ্গুয়েজ: ডেটা টাইপ, অপারেটর, এক্সপ্রেশন, কন্ট্রোল স্ট্রাকচার (যদি-অন্যথায়, সুইচ-কেস, লুপ); ফাংশন এবং প্রোগ্রাম কাঠামো: প্যারামিটার পাসিং কনভেনশন, সুযোগ নিয়ম এবং স্টোরেজ ক্লাস, পুনরাবৃত্তি; হেডার ফাইল; পয়েন্টার এবং অ্যারে; স্ট্রিং; বহুমাত্রিক অ্যারে; ব্যবহারকারী সংজ্ঞায়িত ডেটা টাইপ: কাঠামো, ইউনিয়ন, গণনা; ইনপুট এবং আউটপুট: স্ট্যান্ডার্ড ইনপুট এবং আউটপুট, ফর্ম্যাট ইনপুট এবং আউটপুট, ফাইল অ্যাক্সেস; পরিবর্তনশীল দৈর্ঘ্য আর্গুমেন্ট তালিকা; কমান্ড লাইন পরামিতি; ত্রুটি পরিচালনা; গ্রাফিক্স; যোগসূত্র; লাইব্রেরি ফাংশন।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE1110")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("chris-ried-ieic5Tq8YMk-unsplash (1).jpg")
                                 .trimester(2)
                                 .courseCode("CSE1112")
                                 .titleEn("Structured Programming Language Laboratory")
                                 .titleBn("স্ট্রাকচার্ড প্রোগ্রামিং ল্যাঙ্গুয়েজ ল্যাবরেটরি")
                                 .descriptionEn("Laboratory work based on CSE 1111 with a project work.")
                                 .descriptionBn("একটি প্রকল্পের কাজের সাথে সিএসই ১১১১ এর উপর ভিত্তি করে ল্যাবরেটরির কাজ।")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("CSE1110")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("antoine-dautry-05A-kdOH6Hw-unsplash.jpg")
                                 .trimester(3)
                                 .courseCode("MATH2183")
                                 .titleEn("Calculus and Linear Algebra")
                                 .titleBn("ক্যালকুলাস এবং রৈখিক বীজগণিত")
                                 .descriptionEn("Calculus: Analysis of Function I: Slope and Concavity, Analysis of function II: Relative Extrema and Polynomials, Partial Derivatives, The Chain Rule. Differential Equation: Solution of the differential equations of 1st and 2nd order. Linear Algebra: Solution of different types of system of linear equations. Operations of matrix algebra, trans- position, inversion, rank of matrices. Solution of system of equations by matrix method. Eigen values and Eigen vectors.")
                                 .descriptionBn("ক্যালকুলাস: ফাংশন আইয়ের বিশ্লেষণ: ঢাল এবং কনকাভিটি, ফাংশন দ্বিতীয়টির বিশ্লেষণ: আপেক্ষিক চরম এবং বহুপদী, আংশিক ডেরিভেটিভস, চেইন বিধি। ডিফারেনশিয়াল সমীকরণ: ১ম ও ২য় ক্রমের ডিফারেনশিয়াল সমীকরণের সমাধান। রৈখিক বীজগণিত: রৈখিক সমীকরণের বিভিন্ন ধরণের সিস্টেমের সমাধান। ম্যাট্রিক্স বীজগণিতের অপারেশন, ট্রান্স-পজিশন, বিপরীত, ম্যাট্রিক্সের র্যাঙ্ক। ম্যাট্রিক্স পদ্ধতি দ্বারা সমীকরণ সিস্টেমের সমাধান। আইগেন মান এবং আইগেন ভেক্টর।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("MATH1151")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("taiki-ishikawa-PUXFKuVf_84-unsplash.jpg")
                                 .trimester(3)
                                 .courseCode("CSE1115")
                                 .titleEn("Object Oriented Programming")
                                 .titleBn("অবজেক্ট ওরিয়েন্টেড প্রোগ্রামিং")
                                 .descriptionEn("The content explores the philosophy and advantages of Object-Oriented Programming (OOP) over structured programming, focusing on key concepts like abstraction, encapsulation, classes, objects, and access specifiers. Topics include constructors, destructors, inheritance, polymorphism (overloading, virtual functions, overriding), exceptions, object-oriented I/O, templates, and multi-threaded programming, providing a comprehensive understanding of OOP principles and practices")
                                 .descriptionBn("বিষয়বস্তুটি কাঠামোগত প্রোগ্রামিংয়ের চেয়ে অবজেক্ট-ওরিয়েন্টেড প্রোগ্রামিং (ওওপি) এর দর্শন এবং সুবিধাগুলি অন্বেষণ করে, বিমূর্ততা, এনক্যাপসুলেশন, ক্লাস, অবজেক্ট এবং অ্যাক্সেস স্পেসিফায়ারগুলির মতো মূল ধারণাগুলিতে মনোনিবেশ করে। বিষয়গুলির মধ্যে কনস্ট্রাক্টর, ডেস্ট্রাক্টর, উত্তরাধিকার, পলিমরফিজম (ওভারলোডিং, ভার্চুয়াল ফাংশন, ওভাররাইডিং), ব্যতিক্রম, অবজেক্ট-ওরিয়েন্টেড আই / ও, টেমপ্লেট এবং মাল্টি-থ্রেডেড প্রোগ্রামিং অন্তর্ভুক্ত রয়েছে, যা ওওপি নীতি এবং অনুশীলনগুলির একটি বিস্তৃত বোঝার সরবরাহ করে")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE1111")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("mohammad-rahmani-8qEB0fTe9Vw-unsplash.jpg")
                                 .trimester(3)
                                 .courseCode("CSE1116")
                                 .titleEn("Object Oriented Programming Laboratory")
                                 .titleBn("অবজেক্ট ওরিয়েন্টেড প্রোগ্রামিং ল্যাবরেটরি")
                                 .descriptionEn("Laboratory work based on advanced topics in Object Oriented Programming with a project work.")
                                 .descriptionBn("একটি প্রকল্পের কাজের সাথে অবজেক্ট ওরিয়েন্টেড প্রোগ্রামিংয়ে উন্নত বিষয়গুলির উপর ভিত্তি করে ল্যাবরেটরির কাজ।")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("CSE1112")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("alexander-sinn-YYUM2sNvnvU-unsplash.jpg")
                                 .trimester(3)
                                 .courseCode("CSE1325")
                                 .titleEn("Digital Logic Design")
                                 .titleBn("ডিজিটাল লজিক ডিজাইন")
                                 .descriptionEn("Number systems: Introduction, digital number systems, arithmetic operations; Function minimization techniques: Boolean algebra (identities, functions and manipulation), Canonical and standard forms, minimization techniques; Combinational logic circuits design procedure; Combinational and Arithmetic functions: Arithmetic (adders) and other popular (encoders, decoders, multiplexers, demultiplexers) modules; Sequential circuits and Registers: Sequential logic design procedure, state diagrams, state table, input and output equations, latches, flip-flops, race around problems, design problems, registers, register transfers, counters and their applications.")
                                 .descriptionBn("সংখ্যা সিস্টেম: ভূমিকা, ডিজিটাল নম্বর সিস্টেম, গাণিতিক অপারেশন; ফাংশন মিনিমাইজেশন কৌশল: বুলিয়ান বীজগণিত (পরিচয়, ফাংশন এবং ম্যানিপুলেশন), ক্যানোনিকাল এবং স্ট্যান্ডার্ড ফর্ম, মিনিমাইজেশন কৌশল; সংমিশ্রণ লজিক সার্কিট নকশা পদ্ধতি; সংমিশ্রণ এবং গাণিতিক ফাংশন: পাটিগণিত (সংযোজনকারী) এবং অন্যান্য জনপ্রিয় (এনকোডার, ডিকোডার, মাল্টিপ্লেক্সার, ডিমাল্টিপ্লেক্সার) মডিউল; অনুক্রমিক সার্কিট এবং রেজিস্টার: অনুক্রমিক লজিক ডিজাইন পদ্ধতি, রাষ্ট্র ডায়াগ্রাম, রাষ্ট্র সারণী, ইনপুট এবং আউটপুট সমীকরণ, ল্যাচ, ফ্লিপ-ফ্লপ, সমস্যার চারপাশে জাতি, নকশা সমস্যা, নিবন্ধন, নিবন্ধন স্থানান্তর, কাউন্টার এবং তাদের অ্যাপ্লিকেশন।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("jeswin-thomas-dfRrpfYD8Iw-unsplash.jpg")
                                 .trimester(3)
                                 .courseCode("CSE1326")
                                 .titleEn("Digital Logic Design Laboratory")
                                 .titleBn("ডিজিটাল লজিক ডিজাইন ল্যাবরেটরি")
                                 .descriptionEn("Laboratory work based on CSE 1325.")
                                 .descriptionBn("সিএসই 1325 এর উপর ভিত্তি করে ল্যাবরেটরি কাজ।")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("artturi-jalli-gYrYa37fAKI-unsplash.jpg")
                                 .trimester(4)
                                 .courseCode("PHY2105")
                                 .titleEn("Physics")
                                 .titleBn("পদার্থবিজ্ঞান")
                                 .descriptionEn("[b]Waves and Oscillations[/b]: Periodic motion, SHM, energy calculations, damping, resonance, mechanical waves, acoustic phenomena, Doppler effect, and EM waves.\n" + "[b]Electricity and Magnetism[/b]: Electrostatics (Coulomb's law, Gauss's law, electric potential, capacitance), circuits (Ohm’s law, RC circuits, Kirchhoff’s rules), magnetism (magnetic fields, Biot-Savart law, Ampere’s law, Hall effect), and inductance (Faraday’s law, transformers).\n" + "[b]Quantum Physics[/b]: Quantum theory (photon energy, photoelectric effect, Compton effect, X-rays, De Broglie wavelength), Schrodinger equation, quantum numbers, tunneling, and Bohr's energy quantization.")
                                 .descriptionBn("[খ]তরঙ্গ এবং দোলন[/খ]: পর্যায়ক্রমিক গতি, এসএইচএম, শক্তি গণনা, স্যাঁতসেঁতে, অনুরণন, যান্ত্রিক তরঙ্গ, শাব্দ ঘটনা, ডপলার প্রভাব এবং ইএম তরঙ্গ।\n" + "[খ]বিদ্যুৎ এবং চৌম্বকত্ব[/খ]: ইলেক্ট্রোস্ট্যাটিক্স (কুলম্বের আইন, গাউসের আইন, বৈদ্যুতিক সম্ভাবনা, ক্যাপাসিট্যান্স), সার্কিট (ওহমের আইন, আরসি সার্কিট, কির্ফোফের বিধি), চৌম্বকত্ব (চৌম্বকীয় ক্ষেত্র, বায়োট-সাভার্ট আইন, অ্যাম্পিয়ারের আইন, হল প্রভাব), এবং আনয়ন (ফ্যারাডের আইন, ট্রান্সফরমার)।\n" + "[খ]কোয়ান্টাম পদার্থবিজ্ঞান[/খ]: কোয়ান্টাম তত্ত্ব (ফোটন শক্তি, আলোক তড়িৎ প্রভাব, কম্পটন প্রভাব, এক্স-রে, ডি ব্রগলি তরঙ্গদৈর্ঘ্য), শ্রোডিঙ্গার সমীকরণ, কোয়ান্টাম সংখ্যা, টানেলিং এবং বোরের শক্তি কোয়ান্টাইজেশন।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("sunder-muthukumaran-d7SxBxEAOfU-unsplash.jpg")
                                 .trimester(4)
                                 .courseCode("PHY2106")
                                 .titleEn("Physics Laboratory")
                                 .titleBn("পদার্থবিজ্ঞান  ল্যাবরেটরি")
                                 .descriptionEn("Experiments based on PHY 2105")
                                 .descriptionBn("পিএইচওয়াই 2105 এর উপর ভিত্তি করে পরীক্ষাগুলি")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("rahul-mishra-o4SzxPgMwV8-unsplash.jpg")
                                 .trimester(4)
                                 .courseCode("CSE2118")
                                 .titleEn("Advanced Object Oriented Programming laboratory")
                                 .titleBn("অ্যাডভান্সড অবজেক্ট ওরিয়েন্টেড প্রোগ্রামিং ল্যাবরেটরি")
                                 .descriptionEn("Laboratory work based on advanced topics in Object Oriented Programming with a project work.")
                                 .descriptionBn("একটি প্রকল্পের কাজের সাথে অবজেক্ট ওরিয়েন্টেড প্রোগ্রামিংয়ে উন্নত বিষয়গুলির উপর ভিত্তি করে ল্যাবরেটরির কাজ।")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("CSE1116")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("ernest-brillo-9_vReDaom2Q-unsplash.jpg")
                                 .trimester(4)
                                 .courseCode("EEE2113")
                                 .titleEn("Electrical Circuits")
                                 .titleBn("বৈদ্যুতিক সার্কিট")
                                 .descriptionEn("Fundamental electrical concepts and measuring units, DC voltages, current, resistance and power, laws of electrical circuits and methods of network analysis, principles of DC measuring apparatus, laws of magnetic fields and methods of solving simple magnetic circuits; Alternating current: instantaneous and RMS current, voltage and power, average power combinations of R, L & C circuits, phasor, representation of sinusoidal quantities")
                                 .descriptionBn("মৌলিক বৈদ্যুতিক ধারণা এবং পরিমাপ ইউনিট, ডিসি ভোল্টেজ, বর্তমান, প্রতিরোধের এবং শক্তি, বৈদ্যুতিক সার্কিটের আইন এবং নেটওয়ার্ক বিশ্লেষণের পদ্ধতি, ডিসি পরিমাপ যন্ত্রের নীতি, চৌম্বকীয় ক্ষেত্রের আইন এবং সহজ চৌম্বকীয় সার্কিট সমাধানের পদ্ধতি; বিকল্প বর্তমান: তাত্ক্ষণিক এবং আরএমএস বর্তমান, ভোল্টেজ এবং শক্তি, আর, এল এবং সি সার্কিটের গড় শক্তি সংমিশ্রণ, ফ্যাসোর, সাইনোসয়েডাল পরিমাণের উপস্থাপনা")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("dawid-malecki-fw7lR3ibfpU-unsplash.jpg")
                                 .trimester(4)
                                 .courseCode("MATH2201")
                                 .titleEn("Coordinate Geometry and Vector Analysis")
                                 .titleBn("সমন্বয় জ্যামিতি এবং ভেক্টর বিশ্লেষণ")
                                 .descriptionEn("The content focuses on advanced geometry and calculus, covering conic sections, 3D coordinates, vector operations, and parametric equations. It delves into cylindrical and spherical coordinates, multi-variable integrals, and fundamental calculus concepts like gradients, divergence, curl, and theorems such as Green's, Stokes', and divergence.")
                                 .descriptionBn("সামগ্রীটি উন্নত জ্যামিতি এবং ক্যালকুলাসের উপর দৃষ্টি নিবদ্ধ করে, শঙ্কু বিভাগ, 3 ডি স্থানাঙ্ক, ভেক্টর অপারেশন এবং প্যারামেট্রিক সমীকরণগুলি আচ্ছাদন করে। এটি নলাকার এবং গোলাকার স্থানাঙ্ক, মাল্টি-ভেরিয়েবল ইন্টিগ্রাল এবং গ্রেডিয়েন্টস, ডাইভারজেন্স, কার্ল এবং গ্রিনস, স্টোকস এবং ডাইভারজেন্সের মতো উপপাদ্যগুলির মতো মৌলিক ক্যালকুলাস ধারণাগুলিতে ডুবে যায়।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("MATH1151")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("annie-spratt-_dAnK9GJvdY-unsplash.jpg")
                                 .trimester(5)
                                 .courseCode("SOC2101")
                                 .titleEn("Society, Environment and Engineering Ethics")
                                 .titleBn("সমাজ, পরিবেশ ও প্রকৌশল নীতিশাস্ত্র")
                                 .descriptionEn("Society: emergence of Sociology as moral lessons for society; Basic institutions in society, organization and institutions in society, Types of Society; Culture: basics of culture, elements of culture, cultural change, socialization, and social issues around us; Technology and society: interaction between technology and society; Engineering ethics: understanding ethics, engineering ethics; Moral reasoning and engineering as social experimentation; The engineers’ concern for safety, professional responsibility; Employer authority; Rights of engineers; Global issues; Career choice and professional outlook; Ethical problems are like design problems; Genetically modified objects (GMO); Environment: environment and environmental issues– environmental degradation, waste management and renewable energy; Basic understanding of sustainable development, SDGs, climate change adaptation; Disability and Accessibility.")
                                 .descriptionBn("সমাজ: সমাজের নৈতিক পাঠ হিসাবে সমাজবিজ্ঞানের উত্থান; সমাজে মৌলিক প্রতিষ্ঠান, সমাজে সংগঠন ও প্রতিষ্ঠান, সমাজের প্রকারভেদ; সংস্কৃতি: সংস্কৃতির বুনিয়াদি, সংস্কৃতির উপাদান, সাংস্কৃতিক পরিবর্তন, সামাজিকীকরণ এবং আমাদের চারপাশের সামাজিক সমস্যা; প্রযুক্তি এবং সমাজ: প্রযুক্তি এবং সমাজের মধ্যে মিথস্ক্রিয়া; প্রকৌশল নীতিশাস্ত্র: নীতিশাস্ত্র, প্রকৌশল নীতিশাস্ত্র বোঝা; সামাজিক পরীক্ষা-নিরীক্ষা হিসাবে নৈতিক যুক্তি এবং প্রকৌশল; নিরাপত্তা, পেশাগত দায়বদ্ধতার জন্য প্রকৌশলীদের উদ্বেগ; নিয়োগকর্তা কর্তৃপক্ষ; প্রকৌশলীদের অধিকার; বৈশ্বিক বিষয়; ক্যারিয়ার পছন্দ এবং পেশাদার দৃষ্টিভঙ্গি; নৈতিক সমস্যাগুলি ডিজাইনের সমস্যার মতো; জেনেটিক্যালি মডিফাইড অবজেক্ট (জিএমও); পরিবেশ: পরিবেশ ও পরিবেশগত সমস্যা- পরিবেশগত অবক্ষয়, বর্জ্য ব্যবস্থাপনা এবং পুনর্নবীকরণযোগ্য শক্তি; টেকসই উন্নয়ন, এসডিজি, জলবায়ু পরিবর্তন অভিযোজন সম্পর্কে মৌলিক ধারণা; অক্ষমতা এবং অ্যাক্সেসযোগ্যতা।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("georgia-de-lotz-Ebb8fe-NZtM-unsplash.jpg")
                                 .trimester(5)
                                 .courseCode("MATH2205")
                                 .titleEn("Probability and Statistics")
                                 .titleBn("সম্ভাবনা এবং পরিসংখ্যান")
                                 .descriptionEn("Frequency distribution; Mean, median, mode and other measures of central tendency; Standard deviation and other measures of dispersion; Moments, skewness and kurtosis, correlation and regression analysis; Elementary probability theory and discontinuous probability distribution, e.g., binomial, Poisson and negative binomial; Continuous probability distributions, e.g. normal and exponential; Characteristics of distributions; Elementary sampling theory; Estimation of parameter, Hypothesis testing.")
                                 .descriptionBn("ফ্রিকোয়েন্সি বিতরণ; গড়, মধ্যমা, মোড এবং কেন্দ্রীয় প্রবণতার অন্যান্য পরিমাপ; স্ট্যান্ডার্ড বিচ্যুতি এবং বিচ্ছুরণের অন্যান্য ব্যবস্থা; মুহুর্ত, তির্যক এবং কুর্তোসিস, পারস্পরিক সম্পর্ক এবং রিগ্রেশন বিশ্লেষণ; প্রাথমিক সম্ভাব্যতা তত্ত্ব এবং বিচ্ছিন্ন সম্ভাব্যতা বিতরণ, যেমন, দ্বিপদী, পয়সোঁ এবং নেতিবাচক দ্বিপদী; ক্রমাগত সম্ভাব্যতা বিতরণ, উদাঃ স্বাভাবিক এবং সূচকীয়; বিতরণের বৈশিষ্ট্য; প্রাথমিক নমুনা তত্ত্ব; প্যারামিটারের অনুমান, হাইপোথিসিস পরীক্ষা।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("MATH1151")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("mauro-sbicego-4hfpVsi-gSg-unsplash.jpg")
                                 .trimester(5)
                                 .courseCode("CSE2233")
                                 .titleEn("Theory of Computation")
                                 .titleBn("কম্পিউটেশন তত্ত্ব")
                                 .descriptionEn("Finite Automata: Deterministic finite automata, Non-deterministic finite automata, equivalence and conversion of deterministic and non-deterministic finite automata, pushdown automata. Context free language, context frees grammar. Turing machines: basic machines, configuration, computing with turning machine, combining turning machines.")
                                 .descriptionBn("সসীম অটোমাটা: নির্ধারণবাদী সীমাবদ্ধ স্বয়ংক্রিয়তা, অ-নির্ধারক সীমাবদ্ধ স্বয়ংক্রিয়তা, নির্ধারণবাদী এবং অ-নির্ধারক সীমাবদ্ধ অটোমাটার সমতুল্য এবং রূপান্তর, পুশডাউন অটোমাটা। প্রসঙ্গ মুক্ত ভাষা, প্রসঙ্গ ব্যাকরণ মুক্ত করে। টুরিং মেশিন: বেসিক মেশিন, কনফিগারেশন, টার্নিং মেশিনের সাথে কম্পিউটিং, টার্নিং মেশিনের সংমিশ্রণ।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("claudio-schwarz-fyeOxvYvIyY-unsplash.jpg")
                                 .trimester(5)
                                 .courseCode("CSE2215")
                                 .titleEn("Data Structure and Algorithms – I")
                                 .titleBn("ডেটা স্ট্রাকচার এবং অ্যালগরিদম – I")
                                 .descriptionEn("Internal data representation; Abstract data types; Introduction to algorithms; Asymptotic analysis: growth of functions, O, Ω and Θ notations; Correctness proof and techniques for analysis of algorithms; Master Theorem; Elementary data structures: arrays, linked lists, stacks, queues, trees and tree traversals, graphs and graph representations, heaps, binary search trees; Graph Traversals: DFS, BFS, Applications of DFS and BFS; Sorting: heap sort, merge sort, quick sort, linear-time sorting; Data structures for set operations.")
                                 .descriptionBn("অভ্যন্তরীণ তথ্য উপস্থাপনা; বিমূর্ত তথ্য প্রকার; অ্যালগরিদম পরিচিতি; অ্যাসিম্পটোটিক বিশ্লেষণ: ফাংশনগুলির বৃদ্ধি, ও, Ω এবং Θ স্বরলিপি; অ্যালগরিদম বিশ্লেষণের জন্য সঠিকতা প্রমাণ এবং কৌশল; মাস্টার উপপাদ্য; প্রাথমিক ডেটা স্ট্রাকচার: অ্যারে, লিঙ্কযুক্ত তালিকা, স্ট্যাক, সারি, গাছ এবং গাছের ট্র্যাভারসাল, গ্রাফ এবং গ্রাফ উপস্থাপনা, স্তূপ, বাইনারি অনুসন্ধান গাছ; গ্রাফ ট্র্যাভার্সালস: ডিএফএস, বিএফএস, ডিএফএস এবং বিএফএসের অ্যাপ্লিকেশন; বাছাই: গাদা বাছাই, মার্জ বাছাই, দ্রুত বাছাই, রৈখিক-সময় বাছাই; সেট অপারেশনগুলির জন্য ডেটা স্ট্রাকচার।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE1115")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("choong-deng-xiang--WXQm_NTK0U-unsplash.jpg")
                                 .trimester(5)
                                 .courseCode("CSE2216")
                                 .titleEn("Data Structure and Algorithms – I Laboratory")
                                 .titleBn("ডেটা স্ট্রাকচার এবং অ্যালগরিদম - I ল্যাবরেটরি")
                                 .descriptionEn("Laboratory work based on Data Structures and Algorithms I")
                                 .descriptionBn("ডেটা স্ট্রাকচার এবং অ্যালগরিদম I এর উপর ভিত্তি করে ল্যাবরেটরি কাজ")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("CSE1116")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("samsung-memory-LQCtVtrdOJw-unsplash.jpg")
                                 .trimester(6)
                                 .courseCode("CSE3313")
                                 .titleEn("Computer Architecture")
                                 .titleBn("কম্পিউটার আর্কিটেকচার")
                                 .descriptionEn("Information representation; Measuring performance; Instructions and data access methods: operations and operands of computer hardware, representing instruction, addressing styles; Arithmetic Logic Unit (ALU) operations, floating point operations, designing ALU; Processor design: datapath – single cycle and multicycle implementations; Control Unit design – hardwired and microprogrammed; Pipeline: pipelined datapath and control, hazards; Exceptions; Memory organization: Cache, Concepts of DMA and Interrupts; Buses: overview of computer BUS standards; Multiprocessors: types of multiprocessors, performance, single bus multiprocessors, multiprocessors connected by network, clusters.")
                                 .descriptionBn("তথ্য উপস্থাপনা; কর্মক্ষমতা পরিমাপ; নির্দেশাবলী এবং ডেটা অ্যাক্সেস পদ্ধতি: কম্পিউটার হার্ডওয়্যারের অপারেশন এবং অপারেন্ড, নির্দেশাবলী উপস্থাপন করে, শৈলী সম্বোধন করে; অ্যারিথমেটিক লজিক ইউনিট (এএলইউ) অপারেশন, ফ্লোটিং পয়েন্ট অপারেশন, ডিজাইনিং এএলইউ; প্রসেসর ডিজাইন: ডেটাপাথ - একক চক্র এবং মাল্টিসাইকেল বাস্তবায়ন; কন্ট্রোল ইউনিট ডিজাইন - হার্ডওয়্যারড এবং মাইক্রোপ্রোগ্রামড; পাইপলাইন: পাইপলাইনযুক্ত ডেটাপাথ এবং নিয়ন্ত্রণ, বিপদ; ব্যতিক্রম; মেমরি সংগঠন: ক্যাশে, ডিএমএ ধারণা এবং ইন্টারাপ্টস; বাস: কম্পিউটার বাস মান ওভারভিউ; মাল্টিপ্রসেসর: মাল্টিপ্রসেসরের ধরণ, পারফরম্যান্স, একক বাস মাল্টিপ্রসেসর, নেটওয়ার্ক দ্বারা সংযুক্ত মাল্টিপ্রসেসর, ক্লাস্টার।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE1325")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("vishnu-mohanan-xoOW8cvsWIo-unsplash.jpg")
                                 .trimester(6)
                                 .courseCode("EEE2123")
                                 .titleEn("Electronics")
                                 .titleBn("ইলেকট্রনিকস")
                                 .descriptionEn("Semiconductor diode: materials, energy band, n-type and p-type materials, p-n junction diode, ideal vs practical diode, zener diode, light emitting diode; Diode applications: load-line Analysis, series-parallel dc circuits, AND/OR logic gates, full-wave and half-wave rectification, clipper and clamper circuits; Bipolar junction transistors: device structure and physical operation, current–voltage characteristics, BJT Circuits at DC, BJT as an amplifier and as a switch; MOS field-effect transistors (MOSFETs): device structure and physical operation, current–voltage characteristics, MOSFET circuits at DC, MOSFET as an amplifier and as a switch; CMOS combinational logic circuit design.")
                                 .descriptionBn("সেমিকন্ডাক্টর ডায়োড: উপকরণ, শক্তি ব্যান্ড, এন-টাইপ এবং পি-টাইপ উপকরণ, পি-এন জংশন ডায়োড, আদর্শ বনাম ব্যবহারিক ডায়োড, জেনার ডায়োড, হালকা নির্গমনকারী ডায়োড; ডায়োড অ্যাপ্লিকেশন: লোড-লাইন বিশ্লেষণ, সিরিজ-সমান্তরাল ডিসি সার্কিট, এবং / অথবা লজিক গেট, পূর্ণ-তরঙ্গ এবং অর্ধ-তরঙ্গ সংশোধন, ক্লিপার এবং ক্ল্যাম্পার সার্কিট; বাইপোলার জংশন ট্রানজিস্টর: ডিভাইস গঠন এবং শারীরিক অপারেশন, বর্তমান-ভোল্টেজ বৈশিষ্ট্য, ডিসিতে বিজেটি সার্কিট, বিজেটি একটি পরিবর্ধক হিসাবে এবং একটি সুইচ হিসাবে; এমওএস ফিল্ড-ইফেক্ট ট্রানজিস্টর (এমওএসএফইটি): ডিভাইস স্ট্রাকচার এবং শারীরিক অপারেশন, বর্তমান-ভোল্টেজ বৈশিষ্ট্য, ডিসিতে এমওএসএফইটি সার্কিট, এমওএসএফইটি একটি পরিবর্ধক হিসাবে এবং একটি সুইচ হিসাবে; সিএমওএস সংমিশ্রণ লজিক সার্কিট ডিজাইন।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("EEE2113")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("thisisengineering-Kzx7m3JwkcQ-unsplash.jpg")
                                 .trimester(6)
                                 .courseCode("EEE2124")
                                 .titleEn("Electronics Laboratory")
                                 .titleBn("ইলেকট্রনিক্স ল্যাবরেটরি")
                                 .descriptionEn("Laboratory work based on EEE 2123.")
                                 .descriptionBn("ইইই ২১২৩ এর উপর ভিত্তি করে ল্যাবরেটরির কাজ।")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("EEE2113")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("lukas-blazek-mcSDtbWXUZU-unsplash.jpg")
                                 .trimester(6)
                                 .courseCode("CSE2217")
                                 .titleEn("Data Structure and Algorithms – II")
                                 .titleBn("ডাটা স্ট্রাকচার ও অ্যালগরিদম – ২")
                                 .descriptionEn("Methods for the design of efficient algorithms: divide and conquer, greedy methods, dynamic programming; Graph algorithms: MST algorithms, shortest path algorithms, maximum flow and maximum bipartite matching; Advanced data Structures: balanced binary search trees (AVL trees, red-black trees, splay trees), skip lists, advanced heaps (Fibonacci heaps, binomial heaps); Hashing; String matching algorithms; NP-completeness; NP-hard and NP-complete problems; Coping with hardness: backtracking, branch and bound, approximation algorithms.")
                                 .descriptionBn("দক্ষ অ্যালগরিদম ডিজাইনের পদ্ধতি: বিভক্ত এবং বিজয়, লোভী পদ্ধতি, গতিশীল প্রোগ্রামিং; গ্রাফ অ্যালগরিদম: এমএসটি অ্যালগরিদম, সংক্ষিপ্ততম পথ অ্যালগরিদম, সর্বাধিক প্রবাহ এবং সর্বাধিক দ্বিপক্ষীয় ম্যাচিং; উন্নত ডেটা স্ট্রাকচার: সুষম বাইনারি অনুসন্ধান গাছ (এভিএল গাছ, লাল-কালো গাছ, স্প্লে গাছ), স্কিপ তালিকা, উন্নত স্তূপ (ফিবোনাচ্চি স্তূপ, দ্বিপদী স্তূপ); হাশিং; স্ট্রিং ম্যাচিং অ্যালগরিদম; এনপি-সম্পূর্ণতা; এনপি-হার্ড এবং এনপি-সম্পূর্ণ সমস্যা; কঠোরতার সাথে মোকাবিলা: ব্যাকট্র্যাকিং, শাখা এবং আবদ্ধ, আনুমানিক অ্যালগরিদম।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE2215")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("markus-spiske-iar-afB0QQw-unsplash.jpg")
                                 .trimester(6)
                                 .courseCode("CSE2218")
                                 .titleEn("Data Structure and Algorithms – II Laboratory")
                                 .titleBn("ডেটা স্ট্রাকচার এবং অ্যালগরিদম - ২ ল্যাবরেটরি")
                                 .descriptionEn("Laboratory work based on Data Structures and Algorithms II.")
                                 .descriptionBn("ডেটা স্ট্রাকচার এবং অ্যালগরিদমের উপর ভিত্তি করে ল্যাবরেটরি কাজ।")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("CSE2216")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("kevin-ku-w7ZyuGYNpRQ-unsplash.jpg")
                                 .trimester(7)
                                 .courseCode("CSE3521")
                                 .titleEn("Database Management Systems")
                                 .titleBn("ডাটাবেস ম্যানেজমেন্ট সিস্টেম")
                                 .descriptionEn("Concepts of database systems; Data Models: Entity-Relationship model, Relational model; Query Languages: SQL, Relational algebra, Constraints, View; Security and Integrity Management; Functional dependencies and normalization; Indexing: primary and secondary indexes, B+ trees; Hashing: Static and Dynamic hashing, Collision Problem in Hashing; Transaction management; Recovery: RAID Different levels; File storage management.")
                                 .descriptionBn("ডাটাবেস সিস্টেমের ধারণা; ডেটা মডেল: সত্তা-সম্পর্ক মডেল, রিলেশনাল মডেল; ক্যোয়ারী ভাষা: এসকিউএল, রিলেশনাল বীজগণিত, সীমাবদ্ধতা, ভিউ; নিরাপত্তা ও শুদ্ধাচার ব্যবস্থাপনা; কার্যকরী নির্ভরতা এবং স্বাভাবিককরণ; সূচীকরণ: প্রাথমিক এবং মাধ্যমিক সূচক, বি + গাছ; হ্যাশিং: স্ট্যাটিক এবং ডায়নামিক হ্যাশিং, হ্যাশিংয়ে সংঘর্ষের সমস্যা; লেনদেন ব্যবস্থাপনা; পুনরুদ্ধার: RAID বিভিন্ন মাত্রা; ফাইল স্টোরেজ ম্যানেজমেন্ট।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("mikhail-fesenko-p6YWrjhmjhM-unsplash.jpg")
                                 .trimester(7)
                                 .courseCode("CSE3522")
                                 .titleEn("Database Management Systems Laboratory")
                                 .titleBn("ডাটাবেস ম্যানেজমেন্ট সিস্টেম ল্যাবরেটরি")
                                 .descriptionEn("Laboratory work based on CSE 3521. A project work will be included.")
                                 .descriptionBn("সিএসই ৩৫২১ এর উপর ভিত্তি করে ল্যাবরেটরির কাজ। একটি প্রকল্পের কাজ অন্তর্ভুক্ত করা হবে।")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("andrea-de-santis-zwd435-ewb4-unsplash.jpg")
                                 .trimester(7)
                                 .courseCode("CSE3811")
                                 .titleEn("Artificial Intelligence")
                                 .titleBn("কৃত্রিম বুদ্ধিমত্তা")
                                 .descriptionEn("Survey and concepts in Artificial Intelligence; Problem solving agents; Uninformed and Informed search tech- niques; Local Search Techniques; Game playing; Constraint Satisfaction Problems; Bayesian learning; Supervised Learning: Classification, Perceptrons; Stationary processes and Markov assumptions; Hidden Markov Models; Hu- man Aware AI Systems.")
                                 .descriptionBn("কৃত্রিম বুদ্ধিমত্তার জরিপ এবং ধারণা; সমস্যা সমাধানের এজেন্ট; অজ্ঞাত এবং অবহিত অনুসন্ধান প্রযুক্তিবিদ; স্থানীয় অনুসন্ধান কৌশল; খেলা খেলা; সীমাবদ্ধতা সন্তুষ্টি সমস্যা; বায়েসিয়ান শিক্ষা; তত্ত্বাবধানে শেখা: শ্রেণিবিন্যাস, পারসেপট্রন; স্থির প্রক্রিয়া এবং মার্কভ অনুমান; লুকানো মার্কভ মডেল; হিউম্যান অ্যাওয়ার এআই সিস্টেমস।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("markus-spiske-Skf7HxARcoc-unsplash.jpg")
                                 .trimester(7)
                                 .courseCode("CSE3812")
                                 .titleEn("Artificial Intelligence Laboratory")
                                 .titleBn("আর্টিফিশিয়াল ইন্টেলিজেন্স ল্যাবরেটরি")
                                 .descriptionEn("Laboratory work based on CSE 341.")
                                 .descriptionBn("সিএসই ৩৪১ এর উপর ভিত্তি করে ল্যাবরেটরির কাজ।")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("christopher-gower-m_HRfLhgABo-unsplash.jpg")
                                 .trimester(7)
                                 .courseCode("CSE3411")
                                 .titleEn("System Analysis and Design")
                                 .titleBn("সিস্টেম বিশ্লেষণ এবং নকশা")
                                 .descriptionEn("System Study: concept of system and system study, system organogram, system development life cycle, different types of system, skills of system analyst; Information Gathering: types of information, sources of information, information gathering tools and their competitive analysis; Guidelines to conduct information gathering tools; Feasibility Study: statement of constraints, types of feasibility for IT products, determining best candidate system, SWOT analysis, cash flow and NPV analysis, feasibility Report; System Design: structured and object oriented design using UML; DFD, use case, sequence diagram, state diagram, class diagram, etc using UML tools; Effective designing of input, output and UI; Software Requirement Specifications (SRS); Project deployment: study on project management and tools; Scheduling by Gantt chart, PERT/CPM method, etc; System security, risk management, data migration, training, art of negotiation, etc.")
                                 .descriptionBn("সিস্টেম স্টাডি: সিস্টেম ও সিস্টেম স্টাডির ধারণা, সিস্টেম অর্গানোগ্রাম, সিস্টেম ডেভেলপমেন্ট লাইফ সাইকেল, বিভিন্ন ধরনের সিস্টেম, সিস্টেম অ্যানালিস্টের দক্ষতা; তথ্য সংগ্রহ: তথ্যের ধরণ, তথ্যের উত্স, তথ্য সংগ্রহের সরঞ্জাম এবং তাদের প্রতিযোগিতামূলক বিশ্লেষণ; তথ্য সংগ্রহের সরঞ্জাম পরিচালনার নির্দেশিকা; সম্ভাব্যতা সমীক্ষা: সীমাবদ্ধতার বিবৃতি, আইটি পণ্যগুলির সম্ভাব্যতার ধরণ, সেরা প্রার্থী সিস্টেম নির্ধারণ, এসডব্লিউওটি বিশ্লেষণ, নগদ প্রবাহ এবং এনপিভি বিশ্লেষণ, সম্ভাব্যতা প্রতিবেদন; সিস্টেম ডিজাইন: ইউএমএল ব্যবহার করে কাঠামোগত এবং অবজেক্ট ওরিয়েন্টেড ডিজাইন; ইউএমএল সরঞ্জামগুলি ব্যবহার করে ডিএফডি, কেস, সিকোয়েন্স ডায়াগ্রাম, স্টেট ডায়াগ্রাম, ক্লাস ডায়াগ্রাম ইত্যাদি ব্যবহার করুন; ইনপুট, আউটপুট এবং ইউআই এর কার্যকর নকশা; সফটওয়্যার রিকোয়ারমেন্ট স্পেসিফিকেশন (এসআরএস); প্রকল্প স্থাপনা: প্রকল্প ব্যবস্থাপনা এবং সরঞ্জাম উপর অধ্যয়ন; গ্যান্ট চার্ট, পিইআরটি / সিপিএম পদ্ধতি ইত্যাদি দ্বারা সময়সূচী; সিস্টেম সিকিউরিটি, রিস্ক ম্যানেজমেন্ট, ডাটা মাইগ্রেশন, ট্রেনিং, আর্ট অব নেগোসিয়েশন ইত্যাদি।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("adam-nowakowski-D4LDw5eXhgg-unsplash.jpg")
                                 .trimester(7)
                                 .courseCode("CSE3412")
                                 .titleEn("System Analysis and Design Laboratory")
                                 .titleBn("সিস্টেম বিশ্লেষণ এবং ডিজাইন ল্যাবরেটরি")
                                 .descriptionEn("Laboratory work based on System Analysis and Design.")
                                 .descriptionBn("সিস্টেম বিশ্লেষণ এবং নকশা উপর ভিত্তি করে ল্যাবরেটরি কাজ।")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("olivier-collet-JMwCe3w7qKk-unsplash.jpg")
                                 .trimester(8)
                                 .courseCode("CSE4325")
                                 .titleEn("Microprocessors and Microcontrollers")
                                 .titleBn("মাইক্রোপ্রসেসর এবং মাইক্রোকন্ট্রোলার")
                                 .descriptionEn("Introduction to 16-bit and 32-bit microprocessors: architecture, addressing modes, instruction set (e.g. x86), interrupts, multitasking and virtual memory, paging, cache memory; Interfacing: programmable peripheral interface, direct memory access (DMA), keyboard and display interface, memory chips (e.g. ROM, RAM), clock generator, bus arbiter; Architecture of modern microprocessors: multi processors vs multi-core architecture, hyperthreading technology, turbo boost technology; Introduction to micro-controllers (e.g. ATMega32): architecture, digital interfacing: LEDs, switches, sensors and motors, analog interfacing: introduction to the analog-to-digital converter (ADC) module, serial communication protocols (e.g. SPI, I2C, CANBUS) for embedded system.")
                                 .descriptionBn("16-বিট এবং 32-বিট মাইক্রোপ্রসেসরের ভূমিকা: আর্কিটেকচার, অ্যাড্রেসিং মোড, নির্দেশ সেট (উদাঃ এক্স 86), বাধা, মাল্টিটাস্কিং এবং ভার্চুয়াল মেমরি, পেজিং, ক্যাশে মেমরি; ইন্টারফেসিং: প্রোগ্রামেবল পেরিফেরাল ইন্টারফেস, ডাইরেক্ট মেমরি অ্যাক্সেস (ডিএমএ), কীবোর্ড এবং ডিসপ্লে ইন্টারফেস, মেমরি চিপ (e.g. ROM, র্যাম), ঘড়ি জেনারেটর, বাস আরবিটার; আধুনিক মাইক্রোপ্রসেসরের আর্কিটেকচার: মাল্টি প্রসেসর বনাম মাল্টি-কোর আর্কিটেকচার, হাইপারথ্রেডিং প্রযুক্তি, টার্বো বুস্ট প্রযুক্তি; মাইক্রো-কন্ট্রোলারগুলির পরিচিতি (উদাঃ এটিএমইজিএ 32): আর্কিটেকচার, ডিজিটাল ইন্টারফেসিং: এলইডি, সুইচ, সেন্সর এবং মোটর, এনালগ ইন্টারফেসিং: এনালগ-টু-ডিজিটাল কনভার্টার (এডিসি) মডিউলের ভূমিকা, এম্বেডেড সিস্টেমের জন্য সিরিয়াল যোগাযোগ প্রোটোকল (উদাঃ এসপিআই, আই 2 সি, ক্যানবাস)।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE3313")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("boliviainteligente-LScsYVz5jss-unsplash.jpg")
                                 .trimester(8)
                                 .courseCode("CSE4326")
                                 .titleEn("Microprocessors and Microcontrollers Laboratory")
                                 .titleBn("মাইক্রোপ্রসেসর এবং মাইক্রোকন্ট্রোলার ল্যাবরেটরি")
                                 .descriptionEn("Students will design simple systems using the principles learned in CSE 4325. An introduction to assembly language will be included in this course at the beginning.")
                                 .descriptionBn("শিক্ষার্থীরা সিএসই 4325 এ শেখা নীতিগুলি ব্যবহার করে সহজ সিস্টেমগুলি ডিজাইন করবে। শুরুতেই এই কোর্সে অ্যাসেম্বলি ল্যাঙ্গুয়েজের একটি পরিচিতি অন্তর্ভুক্ত করা হবে।")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("EEE2124")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("growtika-P5mCQ4KACbM-unsplash.jpg")
                                 .trimester(8)
                                 .courseCode("CSE3711")
                                 .titleEn("Computer Networks")
                                 .titleBn("কম্পিউটার নেটওয়ার্ক")
                                 .descriptionEn("Introduction to Computer Networks; Network Edge, Network Core ; Layering architecture: TCP/IP and OSI Reference Models; Circuit Switching and Packet Switching; Hubs, Routers, and Switches; Application layer services: Web, HTTP, FTP, SMTP, DNS architecture; Introduction to transport layer: UDP,TCP; Principles of Reliable data transfer; TCP Congestion and Flow control; Routing and forwarding, DHCP, NAT, Fragmentation; Routing algorithms; Autonomous Systems; Link layer services; MAC Protocols; Link layer addressing; Ethernet; ARP; Wireless links and network characteristics; Wi-Fi: IEEE 802.11 Wireless LANs.")
                                 .descriptionBn("কম্পিউটার নেটওয়ার্ক পরিচিতি; নেটওয়ার্ক এজ, নেটওয়ার্ক কোর; লেয়ারিং আর্কিটেকচার: টিসিপি / আইপি এবং ওএসআই রেফারেন্স মডেল; সার্কিট সুইচিং এবং প্যাকেট সুইচিং; হাব, রাউটার এবং সুইচ; অ্যাপ্লিকেশন স্তর পরিষেবাদি: ওয়েব, এইচটিটিপি, এফটিপি, এসএমটিপি, ডিএনএস আর্কিটেকচার; পরিবহন স্তর ভূমিকা: ইউডিপি, টিসিপি; নির্ভরযোগ্য তথ্য স্থানান্তর নীতি; টিসিপি কনজেশন এবং প্রবাহ নিয়ন্ত্রণ; রাউটিং এবং ফরওয়ার্ডিং, ডিএইচসিপি, ন্যাট, ফ্র্যাগমেন্টেশন; রাউটিং অ্যালগরিদম; স্বায়ত্তশাসিত সিস্টেম; লিংক লেয়ার সেবা; ম্যাক প্রোটোকল; লিঙ্ক স্তর ঠিকানা; ইথারনেট; এআরপি; ওয়্যারলেস লিঙ্ক এবং নেটওয়ার্ক বৈশিষ্ট্য; ওয়াই-ফাই (IEEE 802.11) ওয়্যারলেস ল্যান।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("clint-adair-BW0vK-FA3eg-unsplash.jpg")
                                 .trimester(8)
                                 .courseCode("CSE3712")
                                 .titleEn("Computer Networks Laboratory")
                                 .titleBn("কম্পিউটার নেটওয়ার্ক ল্যাবরেটরি")
                                 .descriptionEn("Laboratory work based on CSE 3711.")
                                 .descriptionBn("সিএসই ৩৭১১ এর উপর ভিত্তি করে ল্যাবরেটরির কাজ।")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("iyus-sugiharto-jpkxJAcp6a4-unsplash.jpg")
                                 .trimester(8)
                                 .courseCode("CSE3421")
                                 .titleEn("Software Engineering")
                                 .titleBn("সফটওয়্যার ইঞ্জিনিয়ারিং")
                                 .descriptionEn("Basic Concepts: software, software engineering, recent trends and challenges; Process Models: waterfall, incremental, iterative; Requirements Engineering: software requirements specification, system requirements specification, stakeholder requirements specification; Architecture: monolithic architecture, service-oriented architecture, micro- service architecture, model-view-controller pattern and variants, system design; Services Computing: application programming interface, web services, cloud services, representational state transfer, JavaScript object notation, simple object access protocol; User Interface Design: web and mobile platform, wireframe model, methods and tools; Software Testing: manual and automated test, black box and white box test, unit test, integration test, regression test, acceptance test, non-functional test, test planning, test documentation; Version Control and Repository: version numbering, version control software, code repository systems; Documentation: requirements, architecture, technical, end user, marketing; Legal and Ethical Aspects: terms and conditions, end-user license agreement, soft- ware engineering code of ethics, privacy engineering; Business Case Study: case study on local and international popular software products.")
                                 .descriptionBn("মৌলিক ধারণা: সফটওয়্যার, সফটওয়্যার ইঞ্জিনিয়ারিং, সাম্প্রতিক প্রবণতা এবং চ্যালেঞ্জ; প্রক্রিয়া মডেল: জলপ্রপাত, ক্রমবর্ধমান, পুনরাবৃত্তি; প্রয়োজনীয়তা প্রকৌশল: সফ্টওয়্যার প্রয়োজনীয়তা স্পেসিফিকেশন, সিস্টেম প্রয়োজনীয়তা স্পেসিফিকেশন, স্টেকহোল্ডার প্রয়োজনীয়তা স্পেসিফিকেশন; স্থাপত্য: মনোলিথিক আর্কিটেকচার, সার্ভিস-ওরিয়েন্টেড আর্কিটেকচার, মাইক্রো-সার্ভিস আর্কিটেকচার, মডেল-ভিউ-কন্ট্রোলার প্যাটার্ন এবং ভেরিয়েন্টস, সিস্টেম ডিজাইন; সার্ভিস কম্পিউটিং: অ্যাপ্লিকেশন প্রোগ্রামিং ইন্টারফেস, ওয়েব সার্ভিসেস, ক্লাউড সার্ভিসেস, রিপ্রেজেন্টেশনাল স্টেট ট্রান্সফার, জাভাস্ক্রিপ্ট অবজেক্ট নোটেশন, সিম্পল অবজেক্ট অ্যাক্সেস প্রোটোকল; ইউজার ইন্টারফেস ডিজাইন: ওয়েব এবং মোবাইল প্ল্যাটফর্ম, ওয়্যারফ্রেম মডেল, পদ্ধতি এবং সরঞ্জাম; সফটওয়্যার টেস্টিং: ম্যানুয়াল এবং স্বয়ংক্রিয় পরীক্ষা, ব্ল্যাক বক্স এবং হোয়াইট বক্স পরীক্ষা, ইউনিট পরীক্ষা, ইন্টিগ্রেশন পরীক্ষা, রিগ্রেশন পরীক্ষা, গ্রহণযোগ্যতা পরীক্ষা, অ-কার্যকরী পরীক্ষা, পরীক্ষা পরিকল্পনা, পরীক্ষা ডকুমেন্টেশন; সংস্করণ নিয়ন্ত্রণ এবং সংগ্রহস্থল: সংস্করণ সংখ্যায়ন, সংস্করণ নিয়ন্ত্রণ সফ্টওয়্যার, কোড সংগ্রহস্থল সিস্টেম; ডকুমেন্টেশন: প্রয়োজনীয়তা, আর্কিটেকচার, প্রযুক্তিগত, শেষ ব্যবহারকারী, বিপণন; আইনি এবং নৈতিক দিক: শর্তাদি এবং শর্তাদি, শেষ ব্যবহারকারী লাইসেন্স চুক্তি, নৈতিকতার নরম-ওয়্যার ইঞ্জিনিয়ারিং কোড, গোপনীয়তা প্রকৌশল; বিজনেস কেস স্টাডি: স্থানীয় এবং আন্তর্জাতিক জনপ্রিয় সফ্টওয়্যার পণ্যগুলির উপর কেস স্টাডি।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE3411")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("szabo-viktor-qmvl6m4Tots-unsplash.jpg")
                                 .trimester(8)
                                 .courseCode("CSE3422")
                                 .titleEn("Software Engineering Laboratory")
                                 .titleBn("সফটওয়্যার ইঞ্জিনিয়ারিং ল্যাবরেটরি")
                                 .descriptionEn("Laboratory work based on Software Engineering.")
                                 .descriptionBn("সফটওয়্যার ইঞ্জিনিয়ারিং উপর ভিত্তি করে ল্যাবরেটরি কাজ।")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("CSE3411")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("robina-weermeijer-Tmkwl7EjVtE-unsplash.jpg")
                                 .trimester(9)
                                 .courseCode("BIO3105")
                                 .titleEn("Biology for Engineers")
                                 .titleBn("জন্য জীববিজ্ঞান প্রকৌশলী")
                                 .descriptionEn("Introduction; The Basics of Life: Chemistry; Organic Molecules: The Molecules of Life; Cell Structure and Function; Enzymes, Coenzymes, and Energy; Biochemical Pathways: Cellular Respiration, Photosynthesis; DNA and RNA: The Molecular Basis of Heredity; Cell Division; Patterns of Inheritance; Applications of Biotechnology; Diversity within Species and Population Genetics; Evolution and Natural Selection; The Formation of Species and Evolutionary Change; Ecosystem Dynamics: The Flow of Energy and Matter; Community Interactions; Population Ecology; Evolutionary and Ecological Aspects of Behavior; The Origin of Life and Evolution of Cells; The Classification and Evolution of Organisms; The Nature of Microorganisms; The Plant Kingdom; The Animal Kingdom; Materials Exchange in the Body; Nutrition: Food and Diet; The Body’s Control Mechanisms and Immunity; Human Reproduction, Sex, and Sexuality.")
                                 .descriptionBn("ভূমিকা; জীবনের মূল বিষয়: রসায়ন; জৈব অণু: জীবনের অণু; সেল গঠন এবং ফাংশন; এনজাইম, কোএনজাইম এবং শক্তি; বায়োকেমিক্যাল পাথওয়েস: সেলুলার শ্বসন, সালোকসংশ্লেষণ; ডিএনএ এবং আরএনএ: বংশগতির আণবিক ভিত্তি; কোষ বিভাজন; উত্তরাধিকারের নিদর্শন; জৈবপ্রযুক্তির প্রয়োগ; প্রজাতি এবং জনসংখ্যা জেনেটিক্সের মধ্যে বৈচিত্র্য; বিবর্তন এবং প্রাকৃতিক নির্বাচন; প্রজাতির গঠন এবং বিবর্তনীয় পরিবর্তন; বাস্তুতন্ত্রের গতিবিদ্যা: শক্তি এবং পদার্থের প্রবাহ; সম্প্রদায় মিথস্ক্রিয়া; জনসংখ্যা বাস্তুশাস্ত্র; আচরণের বিবর্তনীয় এবং পরিবেশগত দিক; জীবনের উৎপত্তি এবং কোষের বিবর্তন; জীবের শ্রেণিবিন্যাস এবং বিবর্তন; অণুজীবের প্রকৃতি; উদ্ভিদ রাজ্য; দ্য অ্যানিম্যাল কিংডম; শরীরের মধ্যে উপকরণ বিনিময়; পুষ্টি: খাদ্য এবং ডায়েট; শরীরের নিয়ন্ত্রণ প্রক্রিয়া এবং অনাক্রম্যতা; মানব প্রজনন, লিঙ্গ এবং যৌনতা।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("scott-graham-5fNmWej4tAA-unsplash.jpg")
                                 .trimester(9)
                                 .courseCode("PMG4101")
                                 .titleEn("Project Management")
                                 .titleBn("প্রকল্প ব্যবস্থাপনা")
                                 .descriptionEn("Triple Constraint in Project Management: Time, Scope and Cost; Process methodology, Requirement Collection, Plan, schedule a project including risk assessment with proper documentation and presentation. Cost Estimation, Optimization, and performance calculation, Change management, Quality improvement, Use of Mod- ern tools in project planning, resource allocation and estimation.")
                                 .descriptionBn("প্রকল্প ব্যবস্থাপনায় ট্রিপল সীমাবদ্ধতা: সময়, সুযোগ এবং ব্যয়; প্রক্রিয়া পদ্ধতি, প্রয়োজনীয়তা সংগ্রহ, পরিকল্পনা, যথাযথ ডকুমেন্টেশন এবং উপস্থাপনা সহ ঝুঁকি মূল্যায়ন সহ একটি প্রকল্পের সময়সূচী। খরচ প্রাক্কলন, অপ্টিমাইজেশান, এবং কর্মক্ষমতা গণনা, পরিবর্তন ব্যবস্থাপনা, গুণমান উন্নতি, প্রকল্প পরিকল্পনা, সম্পদ বরাদ্দ এবং অনুমানের আধুনিক সরঞ্জামগুলির ব্যবহার।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE3411")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("austin-distel-744oGeqpxPQ-unsplash.jpg")
                                 .trimester(101)
                                 .courseCode("ECO4101")
                                 .titleEn("Economics")
                                 .titleBn("অর্থনীতি")
                                 .descriptionEn("Economics studies the allocation of scarce resources to meet human needs, with applications in engineering for optimizing resource use. Principles of economics include microeconomics (economic systems, demand and supply, consumer behavior, production theory, market structures, cost analysis, and optimization) and macroeconomics (savings, investment, employment, national income, inflation, and policies). Microeconomics focuses on resource allocation and production at a firm level, while macroeconomics examines broader economic issues like fiscal and monetary policies, especially in Bangladesh. Economic theories help address fundamental problems and development planning in both developed and developing countries.")
                                 .descriptionBn("অর্থনীতি মানুষের চাহিদা পূরণের জন্য দুর্লভ সম্পদের বরাদ্দ অধ্যয়ন করে, সম্পদ ব্যবহারের অনুকূলকরণের জন্য প্রকৌশলে প্রয়োগ সহ। অর্থনীতির মূলনীতিগুলির মধ্যে রয়েছে মাইক্রোইকোনমিক্স (অর্থনৈতিক ব্যবস্থা, চাহিদা ও সরবরাহ, ভোক্তা আচরণ, উত্পাদন তত্ত্ব, বাজার কাঠামো, ব্যয় বিশ্লেষণ এবং অপ্টিমাইজেশান) এবং সামষ্টিক অর্থনীতি (সঞ্চয়, বিনিয়োগ, কর্মসংস্থান, জাতীয় আয়, মুদ্রাস্ফীতি এবং নীতি)। সামষ্টিক অর্থনীতি একটি দৃঢ় পর্যায়ে সম্পদ বরাদ্দ এবং উৎপাদনের উপর দৃষ্টি নিবদ্ধ করে, যখন সামষ্টিক অর্থনীতি বিশেষত বাংলাদেশে রাজস্ব ও আর্থিক নীতির মতো বৃহত্তর অর্থনৈতিক বিষয়গুলি পরীক্ষা করে। অর্থনৈতিক তত্ত্বগুলি উন্নত ও উন্নয়নশীল উভয় দেশেই মৌলিক সমস্যা এবং উন্নয়ন পরিকল্পনা মোকাবেলায় সহায়তা করে।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("gary-butterfield-YG8rZ323UsU-unsplash.jpg")
                                 .trimester(101)
                                 .courseCode("SOC4101")
                                 .titleEn("Introduction to Sociology")
                                 .titleBn("সমাজবিজ্ঞান পরিচিতি")
                                 .descriptionEn("Concept and theory: major schools of sociology – functionalism, critical theory, gender, interactionism and post- modernism; Sociology of communications: the impacts of contemporary media institutions and communications technologies on the social construction of knowledge and the construction of socially significant identities and ideologies; Society: discussion on key concepts of society, social institutions, social structure and stratification, religion and so on; Sociology of development: technology, gender, business, globalization, and how do we formulate reasonable expectations? Global and social issues; Social research: importance of research, research methods and techniques.")
                                 .descriptionBn("ধারণা এবং তত্ত্ব: সমাজবিজ্ঞানের প্রধান বিদ্যালয় - কার্যকারিতাবাদ, সমালোচনা তত্ত্ব, লিঙ্গ, মিথস্ক্রিয়াবাদ এবং উত্তর-আধুনিকতাবাদ; যোগাযোগের সমাজবিজ্ঞান: জ্ঞানের সামাজিক নির্মাণ এবং সামাজিকভাবে গুরুত্বপূর্ণ পরিচয় এবং মতাদর্শ নির্মাণের উপর সমসাময়িক মিডিয়া প্রতিষ্ঠান এবং যোগাযোগ প্রযুক্তির প্রভাব; সমাজ: সমাজের মূল ধারণা, সামাজিক প্রতিষ্ঠান, সামাজিক কাঠামো এবং স্তরবিন্যাস, ধর্ম ইত্যাদি নিয়ে আলোচনা; উন্নয়নের সমাজবিজ্ঞান: প্রযুক্তি, লিঙ্গ, ব্যবসা, বিশ্বায়ন এবং আমরা কীভাবে যুক্তিসঙ্গত প্রত্যাশা তৈরি করব? বৈশ্বিক ও সামাজিক সমস্যা; সামাজিক গবেষণা: গবেষণা, গবেষণা পদ্ধতি এবং কৌশলগুলির গুরুত্ব।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("microsoft-365-oUbzU87d1Gc-unsplash.jpg")
                                 .trimester(101)
                                 .courseCode("ACT2111")
                                 .titleEn("Financial and Managerial Accounting")
                                 .titleBn("আর্থিক এবং ব্যবস্থাপনাগত অ্যাকাউন্টিং")
                                 .descriptionEn("Financial Accounting: Objectives and importance of accounting; Accounting as an information system; Computerized system and applications in accounting. Recording system: double entry mechanism; accounts and their classification; Accounting equation; Accounting cycle: journal, ledger, trial balance; Preparation of financial statements considering adjusting and closing entries; Accounting concepts (principles) and conventions.\n" + "Financial statement analysis and interpretation: ratio analysis.\n" + "Cost and Management Accounting: Cost concepts and classification; Overhead cost: meaning and classification; Distribution of overhead cost; Overhead recovery method/rate; Job order costing: preparation of job cost sheet and quotation price; Inventory valuation: absorption costing and marginal/variable costing techniques; Cost-Volume- Profit analysis: meaning breakeven analysis, contribution margin approach, sensitivity analysis.\n" + "Short-term investment decisions: relevant and differential cost analysis. Long-term investment decisions: capital budgeting, various techniques of evaluation of capital investments.")
                                 .descriptionBn("আর্থিক হিসাব: অ্যাকাউন্টিংয়ের উদ্দেশ্য এবং গুরুত্ব; একটি তথ্য সিস্টেম হিসাবে অ্যাকাউন্টিং; অ্যাকাউন্টিংয়ে কম্পিউটারাইজড সিস্টেম এবং অ্যাপ্লিকেশন। রেকর্ডিং সিস্টেম: ডাবল এন্ট্রি প্রক্রিয়া; অ্যাকাউন্ট এবং তাদের শ্রেণিবিন্যাস; হিসাববিজ্ঞান সমীকরণ; অ্যাকাউন্টিং চক্র: জার্নাল, লেজার, ট্রায়াল ব্যালেন্স; এন্ট্রি সমন্বয় ও সমাপনী বিবেচনায় আর্থিক বিবরণী প্রণয়ন; অ্যাকাউন্টিং ধারণা (নীতি) এবং নিয়মাবলী।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("taton-moise-EhOCnW4wnuQ-unsplash.jpg")
                                 .trimester(101)
                                 .courseCode("IPE3401")
                                 .titleEn("Industrial and Operational Management")
                                 .titleBn("শিল্প ও কর্মক্ষম ব্যবস্থাপনা")
                                 .descriptionEn("Introduction, evolution, management function, organization and environment.\n" + "\n" + "Organization: Theory and structure; Coordination; Span of control; Authority delegation; Groups; Committee and task force; Manpower planning.\n" + "\n" + "Personnel Management: Scope; Importance; Need hierarchy; Motivation; Job redesign; Leadership; Participative management; Training; Performance appraisal; Wages and incentives; Informal groups; Organizational change and conflict.\n" + "\n" + "Cost and Financial Management; Elements of costs of products depreciation; Break-even analysis; Investment analysis; Benefit cost analysis.\n" + "\n" + "Management Accounting: Cost planning and control; Budget and budgetary control; Development planning process.\n" + "\n" + "Marketing Management: Concepts; Strategy; Sales promotion; Patent laws.\n" + "\n" + "Technology Management: Management of innovation and changes; Technology life cycle; Case studies.")
                                 .descriptionBn("ভূমিকা, বিবর্তন, ব্যবস্থাপনা ফাংশন, সংগঠন এবং পরিবেশ।\n" + "\n" + "সংগঠন: তত্ত্ব এবং কাঠামো; সমন্বয়; নিয়ন্ত্রণের স্প্যান; কর্তৃপক্ষ প্রতিনিধি দল; গোষ্ঠী; কমিটি ও টাস্কফোর্স; জনবল পরিকল্পনা।\n" + "\n" + "কর্মী ব্যবস্থাপনা: সুযোগ; গুরুত্ব; শ্রেণিবিন্যাস প্রয়োজন; প্রেরণা; কাজের পুনর্বিন্যাস; নেতৃত্ব; অংশগ্রহণমূলক ব্যবস্থাপনা; প্রশিক্ষণ; পারফরম্যান্স মূল্যায়ন; মজুরি ও প্রণোদনা; অনানুষ্ঠানিক গোষ্ঠী; সাংগঠনিক পরিবর্তন ও সংঘাত।\n" + "\n" + "খরচ এবং আর্থিক ব্যবস্থাপনা; পণ্য অবচয় খরচ উপাদান; ব্রেক-ইভেন বিশ্লেষণ; বিনিয়োগ বিশ্লেষণ; বেনিফিট খরচ বিশ্লেষণ।\n" + "\n" + "ম্যানেজমেন্ট অ্যাকাউন্টিং: খরচ পরিকল্পনা এবং নিয়ন্ত্রণ; বাজেট ও বাজেট নিয়ন্ত্রণ; উন্নয়ন পরিকল্পনা প্রক্রিয়া।\n" + "\n" + "মার্কেটিং ম্যানেজমেন্ট: কনসেপ্ট; কৌশল; বিক্রয় প্রচার; পেটেন্ট আইন।\n" + "\n" + "প্রযুক্তি ব্যবস্থাপনা: উদ্ভাবন এবং পরিবর্তন পরিচালনা; প্রযুক্তির জীবনচক্র; কেস স্টাডি।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("johannes-plenio-FZpCcPss9to-unsplash.jpg")
                                 .trimester(101)
                                 .courseCode("TEC2499")
                                 .titleEn("Technology Entrepreneurship")
                                 .titleBn("প্রযুক্তি উদ্যোক্তা")
                                 .descriptionEn("Starting a startup involves identifying the right time and resources, forming a team, defining the core idea, and assessing feasibility. The process includes creating a business model, developing a prototype, and testing it with potential customers to gather feedback. Based on feedback, decide whether to pivot or persevere, refining the product and strategy accordingly. Scaling requires efficient production, navigating legal and market challenges, and adapting to evolving technology and market trends. Continuous innovation and competitive response are key to maintaining leadership and success.")
                                 .descriptionBn("একটি স্টার্টআপ শুরু করার মধ্যে সঠিক সময় এবং সংস্থানগুলি চিহ্নিত করা, একটি দল গঠন করা, মূল ধারণাটি সংজ্ঞায়িত করা এবং সম্ভাব্যতার মূল্যায়ন করা জড়িত। প্রক্রিয়াটির মধ্যে একটি ব্যবসায়িক মডেল তৈরি করা, একটি প্রোটোটাইপ বিকাশ করা এবং প্রতিক্রিয়া সংগ্রহের জন্য সম্ভাব্য গ্রাহকদের সাথে এটি পরীক্ষা করা অন্তর্ভুক্ত। প্রতিক্রিয়ার উপর ভিত্তি করে, পিভট বা অধ্যবসায় করবেন কিনা তা সিদ্ধান্ত নিন, সেই অনুযায়ী পণ্য এবং কৌশলটি পরিমার্জন করুন। স্কেলিংয়ের জন্য দক্ষ উত্পাদন, আইনী এবং বাজারের চ্যালেঞ্জগুলি নেভিগেট করা এবং বিকশিত প্রযুক্তি এবং বাজারের প্রবণতাগুলির সাথে খাপ খাইয়ে নেওয়া প্রয়োজন। ক্রমাগত উদ্ভাবন এবং প্রতিযোগিতামূলক প্রতিক্রিয়া নেতৃত্ব এবং সাফল্য বজায় রাখার মূল চাবিকাঠি।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("bret-kavanaugh-_af0_qAh4K4-unsplash.jpg")
                                 .trimester(101)
                                 .courseCode("PSY2101")
                                 .titleEn("Psychology")
                                 .titleBn("মনোবিজ্ঞান")
                                 .descriptionEn("The objective of this course is to provide knowledge about the basic concepts and principles of psychology pertaining to real-life problems. The course will familiarize students with the fundamental processes that occur within organism-biological basis of behavior, perception, motivation, emotion, learning, memory and forgetting and also to the social perspective-social perception and social forces that act upon the individual.")
                                 .descriptionBn("এই কোর্সের উদ্দেশ্য বাস্তব জীবনের সমস্যা সম্পর্কিত মনোবিজ্ঞানের মৌলিক ধারণা এবং নীতি সম্পর্কে জ্ঞান প্রদান করা। কোর্সটি শিক্ষার্থীদের আচরণ, উপলব্ধি, প্রেরণা, আবেগ, শেখার, স্মৃতি এবং ভুলে যাওয়ার জীব-জৈবিক ভিত্তি এবং সামাজিক দৃষ্টিকোণ-সামাজিক উপলব্ধি এবং সামাজিক শক্তির মধ্যে ঘটে যাওয়া মৌলিক প্রক্রিয়াগুলির সাথে পরিচিত করবে।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("sazzad-bin-jafor-pUWmncNmOTo-unsplash.jpg")
                                 .trimester(101)
                                 .courseCode("BDS2201")
                                 .titleEn("Bangladesh Studies")
                                 .titleBn("বাংলাদেশ স্টাডিজ")
                                 .descriptionEn("Ancient Bengal: Sasanka, Rise of the Palas, the Senas; Early Medieval Bengal; Coming of the Muslims; The In- dependent sultanate of Bengal: Ilyas Shahi and Hossein Shahi Bengal; Late medieval Bengal: The Establishment of Mughal Rule in Bengal; Bara Bhuiyans: Subedars and Nawabs; The European Style in Bengal Architecture; British rule in Bengal; Battles of Plassey and Buzas; The Dual government; permanent settlement (1793); Nine- teenth century Bengali renaissance: social and religious reforms, Raja Rammohan Roy, Ishwar Chandra Vidyasagar, Titu Meer; Partition of Bengal (1905); Language Movement (1952); Movement for Autonomy; 6-point and 11-Point Programs; The 1970 Election-Military Action, Genocide in the East Pakistan; The Liberation War; The Emergence of Bangladesh as a Sovereign Independent State in 1971; Culture: Cultural diffusion and change, Bengali culture and problems of society; social problems of Bangladesh; Social change: theories of social change; social change in Bangladesh; urbanization process and its impact on Bangladesh society.")
                                 .descriptionBn("প্রাচীন বাংলা: শশাঙ্ক, পালদের উত্থান, সেন; আদি মধ্যযুগীয় বাংলা; মুসলমানদের আগমন; বাংলার নির্ভরশীল সালতানাত: ইলিয়াস শাহী ও হোসেন শাহী বেঙ্গল; মধ্যযুগের শেষের বাংলা: বাংলায় মুঘল শাসন প্রতিষ্ঠা; বারো ভূঁইয়া: সুবেদার ও নবাব; বাংলার স্থাপত্যশৈলীতে ইউরোপীয় শৈলী; বাংলায় ব্রিটিশ শাসন; পলাশী ও বুজাসের যুদ্ধ; দ্বৈত সরকার; চিরস্থায়ী বন্দোবস্ত (১৭৯৩); উনিশ শতকের বাঙালির নবজাগরণ: সামাজিক ও ধর্মীয় সংস্কার, রাজা রামমোহন রায়, ঈশ্বরচন্দ্র বিদ্যাসাগর, টিটু মীর; বঙ্গভঙ্গ (১৯০৫); ভাষা আন্দোলন (১৯৫২); স্বায়ত্তশাসনের জন্য আন্দোলন; ৬ দফা ও ১১ দফা কর্মসূচি; ১৯৭০ সালের নির্বাচন-সামরিক পদক্ষেপ, পূর্ব পাকিস্তানে গণহত্যা; মুক্তিযুদ্ধ; ১৯৭১ সালে একটি সার্বভৌম স্বাধীন রাষ্ট্র হিসেবে বাংলাদেশের অভ্যুদয়; সংস্কৃতি: সাংস্কৃতিক বিস্তার ও পরিবর্তন, বাঙালি সংস্কৃতি ও সমাজের সমস্যা; বাংলাদেশের সামাজিক সমস্যা; সামাজিক পরিবর্তন: সামাজিক পরিবর্তনের তত্ত্ব; বাংলাদেশের সামাজিক পরিবর্তন; নগরায়ন প্রক্রিয়া এবং বাংলাদেশের সমাজে এর প্রভাব।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("al-amin-mir-JCtBOfMFc4c-unsplash.jpg")
                                 .trimester(101)
                                 .courseCode("BAN2501")
                                 .titleEn("Bangla")
                                 .titleBn("বাংলা")
                                 .descriptionEn("")
                                 .descriptionBn("")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("fahim-muntashir-aCeVa_v7OYg-unsplash.jpg")
                                 .trimester(10)
                                 .courseCode("CSE4000A")
                                 .titleEn("Final Year Design Project – I")
                                 .titleBn("ফাইনাল ইয়ার ডিজাইন প্রজেক্ট – ১")
                                 .descriptionEn("This course introduce different soft skill-sets that are necessary for the successful completion of FYDP. The skill- sets include, but not limited to, mastering effective communications, individual and team development, ethical leadership, project management, the steps in the design process, environment and sustainability, etc. These skill sets would be developed by a series of seminars and workshops. The outcomes relevant to POs would be measured based on the student performance in different tests designed to assess those specific skills. The standard rubrics will be used to assess the performance. At the end of the trimester the students will submit an interim report of their FYDP and give a presentation.")
                                 .descriptionBn("এই কোর্সটি এফওয়াইডিপির সফল সমাপ্তির জন্য প্রয়োজনীয় বিভিন্ন নরম দক্ষতা-সেট প্রবর্তন করে। দক্ষতা-সেটগুলির মধ্যে কার্যকর যোগাযোগ, স্বতন্ত্র এবং দলগত বিকাশ, নৈতিক নেতৃত্ব, প্রকল্প পরিচালনা, নকশা প্রক্রিয়ার পদক্ষেপ, পরিবেশ এবং স্থায়িত্ব ইত্যাদি অন্তর্ভুক্ত রয়েছে তবে সীমাবদ্ধ নয়। এই দক্ষতার সেটগুলি একাধিক সেমিনার এবং কর্মশালার মাধ্যমে বিকাশ করা হবে। পিওগুলির সাথে প্রাসঙ্গিক ফলাফলগুলি সেই নির্দিষ্ট দক্ষতাগুলি মূল্যায়ন করার জন্য ডিজাইন করা বিভিন্ন পরীক্ষায় শিক্ষার্থীদের পারফরম্যান্সের ভিত্তিতে পরিমাপ করা হবে। স্ট্যান্ডার্ড রুব্রিকগুলি কর্মক্ষমতা মূল্যায়ন করতে ব্যবহৃত হবে। ত্রৈমাসিকের শেষে শিক্ষার্থীরা তাদের এফওয়াইডিপির একটি অন্তর্বর্তীকালীন প্রতিবেদন জমা দেবে এবং একটি উপস্থাপনা দেবে।")
                                 .type("Theory")
                                 .credits(2)
                                 .prerequisite("PMG4101, SOC2101")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("crew-4Hg8LH9Hoxc-unsplash.jpg")
                                 .trimester(10)
                                 .courseCode("CSE3509")
                                 .titleEn("Operating Systems")
                                 .titleBn("অপারেটিং সিস্টেম")
                                 .descriptionEn("Operating system: its role in computer systems; multitasking, multiuser, multiprocessing OS; Operating system structures; Process: process concept and scheduling, inter-process communication, communication in client-server systems; CPU scheduling: scheduling criteria and algorithms, thread scheduling, multiple-processor scheduling; Process synchronization: critical-section problem, semaphores, monitors; Deadlock: resource allocation and dead- lock, deadlock detection, prevention and recovery; Memory management: swapping, paging, segmentation, virtual memory; File Systems: files, directories, security, protection; Case study of some operating systems.")
                                 .descriptionBn("অপারেটিং সিস্টেম: কম্পিউটার সিস্টেমে এর ভূমিকা; মাল্টিটাস্কিং, মাল্টিইউজার, মাল্টিপ্রসেসিং ওএস; অপারেটিং সিস্টেম কাঠামো; প্রক্রিয়া: প্রক্রিয়া ধারণা এবং সময়সূচী, আন্তঃপ্রক্রিয়া যোগাযোগ, ক্লায়েন্ট-সার্ভার সিস্টেমে যোগাযোগ; সিপিইউ সময়সূচী: সময়সূচী মানদণ্ড এবং অ্যালগরিদম, থ্রেড সময়সূচী, একাধিক-প্রসেসর সময়সূচী; প্রক্রিয়া সিঙ্ক্রোনাইজেশন: সমালোচনামূলক-বিভাগ সমস্যা, সেমাফোরস, মনিটর; অচলাবস্থা: সম্পদ বরাদ্দ এবং ডেড-লক, ডেডলক সনাক্তকরণ, প্রতিরোধ এবং পুনরুদ্ধার; মেমরি ম্যানেজমেন্ট: সোয়াপিং, পেজিং, সেগমেন্টেশন, ভার্চুয়াল মেমরি; ফাইল সিস্টেম: ফাইল, ডিরেক্টরি, নিরাপত্তা, সুরক্ষা; কিছু অপারেটিং সিস্টেমের কেস স্টাডি।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("national-cancer-institute-rUfUd-7WW78-unsplash.jpg")
                                 .trimester(10)
                                 .courseCode("CSE3510")
                                 .titleEn("Operating Systems Laboratory")
                                 .titleBn("অপারেটিং সিস্টেম ল্যাবরেটরি")
                                 .descriptionEn("Laboratory work based on Operating System Concepts")
                                 .descriptionBn("অপারেটিং সিস্টেম ধারণার উপর ভিত্তি করে ল্যাবরেটরি কাজ")
                                 .type("Lab")
                                 .credits(1)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("mark-kats-rN5lr7o9-c0-unsplash.jpg")
                                 .trimester(11)
                                 .courseCode("CSE4000B")
                                 .titleEn("Final Year Design Project – II")
                                 .titleBn("ফাইনাল ইয়ার ডিজাইন প্রজেক্ট – ২")
                                 .descriptionEn("In this course, the students will implement the proposal that is accepted in the course CSE 4000A.")
                                 .descriptionBn("এই কোর্সে, শিক্ষার্থীরা সিএসই 4000A কোর্সে গৃহীত প্রস্তাবটি বাস্তবায়ন করবে।")
                                 .type("Theory")
                                 .credits(2)
                                 .prerequisite("CSE4000A")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("glen-carrie-SCwbIVG4vhA-unsplash.jpg")
                                 .trimester(11)
                                 .courseCode("CSE4531")
                                 .titleEn("Computer Security")
                                 .titleBn("কম্পিউটার নিরাপত্তা")
                                 .descriptionEn("Fundamental concepts: confidentiality, integrity and availability, assurance, authenticity and anonymity; threats and attacks, security principles; Encryption, symmetric and asymmetric key encryption; Security: OS access control, Web and mobile application security, software security, hardware security, memory protection, data base security; Security Attacks: malware, DDos, Trojan and backdoors, buffer overflow, social engineering.")
                                 .descriptionBn("মৌলিক ধারণা: গোপনীয়তা, অখণ্ডতা এবং প্রাপ্যতা, আশ্বাস, সত্যতা এবং নামহীনতা; হুমকি এবং আক্রমণ, নিরাপত্তা নীতি; এনক্রিপশন, প্রতিসম এবং অসমমিতিক কী এনক্রিপশন; সিকিউরিটি: ওএস এক্সেস কন্ট্রোল, ওয়েব অ্যান্ড মোবাইল অ্যাপ্লিকেশন সিকিউরিটি, সফটওয়্যার সিকিউরিটি, হার্ডওয়্যার সিকিউরিটি, মেমরি প্রোটেকশন, ডাটা বেস সিকিউরিটি; নিরাপত্তা আক্রমণ: ম্যালওয়্যার, ডিডস, ট্রোজান এবং ব্যাকডোর, বাফার ওভারফ্লো, সোশ্যাল ইঞ্জিনিয়ারিং।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE3711")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("becomes-co-7oBmQz4bfrQ-unsplash.jpg")
                                 .trimester(12)
                                 .courseCode("CSE4000C")
                                 .titleEn("Final Year Design Project – III")
                                 .titleBn("ফাইনাল ইয়ার ডিজাইন প্রজেক্ট – ৩")
                                 .descriptionEn("In this course, the students will implement the proposal that is accepted in the course CSE 4000B.")
                                 .descriptionBn("এই কোর্সে, শিক্ষার্থীরা সিএসই 4000 বি কোর্সে গৃহীত প্রস্তাবটি বাস্তবায়ন করবে।")
                                 .type("Theory")
                                 .credits(2)
                                 .prerequisite("CSE4000 B")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("gavin-allanwood-IHfiV4rquMY-unsplash.jpg")
                                 .trimester(12)
                                 .courseCode("EEE4261")
                                 .titleEn("Green Computing")
                                 .titleBn("গ্রিন কম্পিউটিং")
                                 .descriptionEn("Cloud computing: Definition, Concept, service model and their clarification, deployment model, security and privacy; Edge Computing: Definition, Concept, Advantages and challenges; Tele-computing: Definition, advantages and challenges; Power and energy management: IEEE rules and codes in power and energy management, Microsoft, IBM and others definition in energy management; E-waste: Definition of e-waste and its recycle process. Cost benefit analysis of e-waste recycle. And environmental impact analysis of e-waste.")
                                 .descriptionBn("ক্লাউড কম্পিউটিং: সংজ্ঞা, ধারণা, পরিষেবা মডেল এবং তাদের স্পষ্টকরণ, স্থাপনার মডেল, সুরক্ষা এবং গোপনীয়তা; এজ কম্পিউটিং: সংজ্ঞা, ধারণা, সুবিধা এবং চ্যালেঞ্জ; টেলি-কম্পিউটিং: সংজ্ঞা, সুবিধা এবং চ্যালেঞ্জ; পাওয়ার অ্যান্ড এনার্জি ম্যানেজমেন্ট: আইইইই রুলস অ্যান্ড কোডস ইন পাওয়ার অ্যান্ড এনার্জি ম্যানেজমেন্ট, মাইক্রোসফট, আইবিএম অ্যান্ড এনার্জি ম্যানেজমেন্টের অন্যান্য সংজ্ঞা; ই-বর্জ্য: ই-বর্জ্যের সংজ্ঞা এবং এর পুনর্ব্যবহার প্রক্রিয়া। ই-বর্জ্য পুনর্ব্যবহারের ব্যয় বেনিফিট বিশ্লেষণ। এবং ই-বর্জ্যের পরিবেশগত প্রভাব বিশ্লেষণ।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("maik-jonietz-_yMciiStJyY-unsplash.jpg")
                                 .trimester(102)
                                 .courseCode("CSE4165")
                                 .titleEn("Web Programming")
                                 .titleBn("ওয়েব প্রোগ্রামিং")
                                 .descriptionEn("Web architecture and HTTP: History and architecture of the World Wide Web, overview of the Hyper Text Transfer Protocol, other related protocols; Hyper Text Markup Language: The concept of markup, overview of HTML ( table, form, frame, window, link etc.); Cascading Style Sheets: Overview of CSS (selectors, different CSS properties and values); Client side scripting: Variables, data types, control structure, functions, Document Object Model (DOM), event handlers, properties, methods, cookies; Server side scripting: Concepts, variables, data types, control structure, functions, objects, regular expressions, mails, cookies, sessions and a related web framework; Database: Content generation, data exchange; Layered or Multi-tier Architecture for Web Applications; MVC; Content Management System.")
                                 .descriptionBn("ওয়েব আর্কিটেকচার এবং এইচটিটিপি: ওয়ার্ল্ড ওয়াইড ওয়েবের ইতিহাস এবং আর্কিটেকচার, হাইপার টেক্সট ট্রান্সফার প্রোটোকলের ওভারভিউ, অন্যান্য সম্পর্কিত প্রোটোকল; হাইপার টেক্সট মার্কআপ ল্যাঙ্গুয়েজ: মার্কআপের ধারণা, এইচটিএমএল এর ওভারভিউ (টেবিল, ফর্ম, ফ্রেম, উইন্ডো, লিঙ্ক ইত্যাদি); ক্যাসকেডিং স্টাইল শীট: সিএসএস (নির্বাচক, বিভিন্ন সিএসএস বৈশিষ্ট্য এবং মান) এর ওভারভিউ; ক্লায়েন্ট সাইড স্ক্রিপ্টিং: ভেরিয়েবল, ডেটা টাইপ, কন্ট্রোল স্ট্রাকচার, ফাংশন, ডকুমেন্ট অবজেক্ট মডেল (ডিওএম), ইভেন্ট হ্যান্ডলার, বৈশিষ্ট্য, পদ্ধতি, কুকিজ; সার্ভার সাইড স্ক্রিপ্টিং: ধারণা, ভেরিয়েবল, ডেটা টাইপ, কন্ট্রোল স্ট্রাকচার, ফাংশন, অবজেক্ট, নিয়মিত এক্সপ্রেশন, মেইল, কুকিজ, সেশন এবং একটি সম্পর্কিত ওয়েব ফ্রেমওয়ার্ক; ডাটাবেস: কনটেন্ট জেনারেশন, ডাটা এক্সচেঞ্জ; ওয়েব অ্যাপ্লিকেশনের জন্য স্তরযুক্ত বা মাল্টি-টায়ার আর্কিটেকচার; এমভিসি; কন্টেন্ট ম্যানেজমেন্ট সিস্টেম।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE 2118")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("william-hook-9e9PD9blAto-unsplash.jpg")
                                 .trimester(102)
                                 .courseCode("CSE4181")
                                 .titleEn("Mobile Application Development")
                                 .titleBn("মোবাইল অ্যাপ্লিকেশন ডেভেলপমেন্ট")
                                 .descriptionEn("Web architecture and HTTP: History and architecture of the World Wide Web, overview of the Hyper Text Transfer Protocol, other related protocols; Hyper Text Markup Language: The concept of markup, overview of HTML ( table, form, frame, window, link etc.); Cascading Style Sheets: Overview of CSS (selectors, different CSS properties and values); Client side scripting: Variables, data types, control structure, functions, Document Object Model (DOM), event handlers, properties, methods, cookies; Server side scripting: Concepts, variables, data types, control structure, functions, objects, regular expressions, mails, cookies, sessions and a related web framework; Database: Content generation, data exchange; Layered or Multi-tier Architecture for Web Applications; MVC; Content Management System.")
                                 .descriptionBn("ওয়েব আর্কিটেকচার এবং এইচটিটিপি: ওয়ার্ল্ড ওয়াইড ওয়েবের ইতিহাস এবং আর্কিটেকচার, হাইপার টেক্সট ট্রান্সফার প্রোটোকলের ওভারভিউ, অন্যান্য সম্পর্কিত প্রোটোকল; হাইপার টেক্সট মার্কআপ ল্যাঙ্গুয়েজ: মার্কআপের ধারণা, এইচটিএমএল এর ওভারভিউ (টেবিল, ফর্ম, ফ্রেম, উইন্ডো, লিঙ্ক ইত্যাদি); ক্যাসকেডিং স্টাইল শীট: সিএসএস (নির্বাচক, বিভিন্ন সিএসএস বৈশিষ্ট্য এবং মান) এর ওভারভিউ; ক্লায়েন্ট সাইড স্ক্রিপ্টিং: ভেরিয়েবল, ডেটা টাইপ, কন্ট্রোল স্ট্রাকচার, ফাংশন, ডকুমেন্ট অবজেক্ট মডেল (ডিওএম), ইভেন্ট হ্যান্ডলার, বৈশিষ্ট্য, পদ্ধতি, কুকিজ; সার্ভার সাইড স্ক্রিপ্টিং: ধারণা, ভেরিয়েবল, ডেটা টাইপ, কন্ট্রোল স্ট্রাকচার, ফাংশন, অবজেক্ট, নিয়মিত এক্সপ্রেশন, মেইল, কুকিজ, সেশন এবং একটি সম্পর্কিত ওয়েব ফ্রেমওয়ার্ক; ডাটাবেস: কনটেন্ট জেনারেশন, ডাটা এক্সচেঞ্জ; ওয়েব অ্যাপ্লিকেশনের জন্য স্তরযুক্ত বা মাল্টি-টায়ার আর্কিটেকচার; এমভিসি; কন্টেন্ট ম্যানেজমেন্ট সিস্টেম।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE 2118")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("chris-liverani-dBI_My696Rk-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4601")
                                 .titleEn("Mathematical Analysis for Computer Science")
                                 .titleBn("কম্পিউটার বিজ্ঞানের জন্য গাণিতিক বিশ্লেষণ")
                                 .descriptionEn("Recurrent problems; Manipulation of sums; Integer functions; Number theory; Binomial coefficient; Special numbers; Generating functions; Combinatorial game theory; Introduction to probability theory, expectation; Random variables; Conditional Probability and Conditional Expectation; Stochastic process; Markov chains: discrete parameter, continuous parameter, birth-death process; Queuing models: birth-death model, Markovian model, open and closed queuing network; Application of queuing models.")
                                 .descriptionBn("পুনরাবৃত্ত সমস্যা; অঙ্কের হেরফের করা; পূর্ণসংখ্যা ফাংশন; সংখ্যা তত্ত্ব; দ্বিপদী সহগ; বিশেষ সংখ্যা; ফাংশন উৎপন্ন করা; কম্বিনেটরিয়াল গেম থিওরি; সম্ভাব্যতা তত্ত্বের ভূমিকা, প্রত্যাশা; র্যান্ডম ভেরিয়েবল; শর্তসাপেক্ষ সম্ভাব্যতা এবং শর্তসাপেক্ষ প্রত্যাশা; স্টোকাস্টিক প্রক্রিয়া; মার্কভ চেইন: বিচ্ছিন্ন পরামিতি, ক্রমাগত পরামিতি, জন্ম-মৃত্যু প্রক্রিয়া; সারি মডেল: জন্ম-মৃত্যু মডেল, মার্কোভিয়ান মডেল, খোলা এবং বন্ধ সারিবদ্ধ নেটওয়ার্ক; সারিবদ্ধ মডেল প্রয়োগ।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("campaign-creators-pypeCEaJeZY-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4633")
                                 .titleEn("Basic Graph Theory")
                                 .titleBn("বেসিক গ্রাফ তত্ত্ব")
                                 .descriptionEn("Graphs and their applications; Basic graph terminologies; Basic operations on graphs; Graph representations; Degree sequence and graphic sequence; Paths, cycles and connectivity; Trees and counting of trees; Distance in graphs and trees; Spanning trees in graphs; Euler tours; Hamiltonian cycles; Ear decomposition; Graph labeling; Matching and Covering: Vertex and Edge Covering; Line graphs, Perfect graphs and Planar graphs; Graph coloring: Vertex coloring and Edge coloring; Special classes of graphs.")
                                 .descriptionBn("গ্রাফ এবং তাদের অ্যাপ্লিকেশন; মৌলিক গ্রাফ পরিভাষা; গ্রাফ উপর মৌলিক অপারেশন; গ্রাফ উপস্থাপনা; ডিগ্রি ক্রম এবং গ্রাফিক ক্রম; পথ, চক্র এবং সংযোগ; গাছ ও গাছের গণনা; গ্রাফ এবং গাছের মধ্যে দূরত্ব; গ্রাফে গাছে ছড়িয়ে পড়া; অয়লার ট্যুর; হ্যামিল্টনীয় চক্র; কানের পচন; গ্রাফ লেবেলিং; ম্যাচিং এবং কভারিং: শীর্ষবিন্দু এবং প্রান্ত আচ্ছাদন; লাইন গ্রাফ, পারফেক্ট গ্রাফ এবং প্ল্যানার গ্রাফ; গ্রাফ রঙিন: শীর্ষবিন্দু রঙ এবং প্রান্ত রঙ; গ্রাফের বিশেষ ক্লাস।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("thisisengineering-hoivM01c-vg-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4655")
                                 .titleEn("Algorithm Engineering")
                                 .titleBn("অ্যালগরিদম ইঞ্জিনিয়ারিং")
                                 .descriptionEn("Computational complexity; Exact Algorithms; Parameterized complexity; Practical computing and heuristics; Approximation algorithms; LP based approximation algorithms; Randomized algorithms; On-line algorithms; Experimental algorithmics; Contemporary and state-of-the-art algorithms.")
                                 .descriptionBn("কম্পিউটেশনাল জটিলতা; সঠিক অ্যালগরিদম; প্যারামিটারাইজড জটিলতা; ব্যবহারিক কম্পিউটিং এবং হিউরিস্টিক্স; আনুমানিক অ্যালগরিদম; এলপি ভিত্তিক আনুমানিক অ্যালগরিদম; এলোমেলো অ্যালগরিদম; অন-লাইন অ্যালগরিদম; পরীক্ষামূলক অ্যালগরিদমিক; সমসাময়িক এবং অত্যাধুনিক অ্যালগরিদম।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("ben-kolde-bs2Ba7t69mM-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4611")
                                 .titleEn("Compiler Design")
                                 .titleBn("কম্পাইলার ডিজাইন")
                                 .descriptionEn("Compiler modules; Lexical analysis; Parsing theory; Symbol tables; Type systems; Scope; Semantic analysis; Intermediate representations; Runtime environments; Code generation; Code optimization.")
                                 .descriptionBn("কম্পাইলার মডিউল; আভিধানিক বিশ্লেষণ; পার্সিং তত্ত্ব; প্রতীক টেবিল; টাইপ সিস্টেম; সুযোগ; শব্দার্থিক বিশ্লেষণ; মধ্যবর্তী উপস্থাপনা; রানটাইম পরিবেশ; কোড জেনারেশন; কোড অপ্টিমাইজেশান।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("wim-van-t-einde-e6pPIcJ05Jg-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4613")
                                 .titleEn("Computational Geometry")
                                 .titleBn("কম্পিউটেশনাল জ্যামিতি")
                                 .descriptionEn("Searching and Geometric Data Structures: Balanced binary search trees, Priority-search trees, Range searching, Interval trees, Segment trees; Algorithms and complexity of fundamental geometric objects: Polygon triangulation and art gallery theorem, Polygon partitioning, Convex-hulls in 2-dimension and 3-dimension, Dynamic convex-hulls; Geometric intersection: Line segment intersection and the plane-sweep algorithm, Intersection of polygons; Prox- imity: Voronoi diagrams, Delunay triangulations, Closest and furthest pair; Visualization: Hidden surface removal and binary space partition (BSP) trees; Graph Drawings: Drawings of rooted trees (Layering, Radial drawings, HV- Drawings, Recursive winding), Drawings of planar graphs (Straight-line drawings, Orthogonal drawings, Visibility drawings).")
                                 .descriptionBn("অনুসন্ধান এবং জ্যামিতিক ডেটা স্ট্রাকচার: সুষম বাইনারি অনুসন্ধান গাছ, অগ্রাধিকার-অনুসন্ধান গাছ, পরিসীমা অনুসন্ধান, ব্যবধান গাছ, সেগমেন্ট গাছ; মৌলিক জ্যামিতিক বস্তুর অ্যালগরিদম এবং জটিলতা: বহুভুজ ত্রিভুজ এবং আর্ট গ্যালারী উপপাদ্য, বহুভুজ বিভাজন, 2-মাত্রা এবং 3-মাত্রায় উত্তল-হাল, গতিশীল উত্তল-হাল; জ্যামিতিক ছেদ: লাইন সেগমেন্ট ছেদ এবং প্লেন-সুইপ অ্যালগরিদম, বহুভুজের ছেদ; প্রক্সি- ইমিটি: ভোরোনয় ডায়াগ্রাম, ডেলুনাই ত্রিভুজাকার, নিকটতম এবং দূরতম জোড়া; ভিজ্যুয়ালাইজেশন: লুকানো পৃষ্ঠ অপসারণ এবং বাইনারি স্পেস পার্টিশন (বিএসপি) গাছ; গ্রাফ অঙ্কন: শিকড়যুক্ত গাছের অঙ্কন (লেয়ারিং, রেডিয়াল অঙ্কন, এইচভি- অঙ্কন, পুনরাবৃত্তিমূলক ঘুর), প্ল্যানার গ্রাফের অঙ্কন (সরল-রেখা অঙ্কন, অর্থোগোনাল অঙ্কন, দৃশ্যমানতা অঙ্কন)।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("allison-saeng-brbSJP4AyJM-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4621")
                                 .titleEn("Computer Graphics")
                                 .titleBn("কম্পিউটার গ্রাফিক্স")
                                 .descriptionEn("Basics of computer graphics and its applications; Raster graphics: 3D rasterization pipeline; Transformation: modelling, viewing and projection transformation in both 2D and 3D spaces; homogeneous coordinate system; Visible surface detection and hidden surface removal: e.g. z-buffer (or, depth buffer), depth-sorting, BSP-tree algorithms; Scan conversion and clipping algorithms: e.g. Cohen-Sutherland, Cyrus-Beck, Sutherland-Hodgman algorithms; Fractals: e.g. Koch curve, Snowflakes, Dragon curve; Ray tracing: ray casting methods, direct illumination, global illumination, shadows, shading and textures.")
                                 .descriptionBn("অ্যাপ্লিকেশনগুলির মূল বিষয়গুলি; রাস্টার গ্রাফিক্স: 3 ডি রাস্টারাইজেশন পাইপলাইন; রূপান্তর: 2 ডি এবং 3 ডি উভয় স্পেসে মডেলিং, দেখার এবং অভিক্ষেপ রূপান্তর; সমজাতীয় স্থানাঙ্ক ব্যবস্থা; দৃশ্যমান পৃষ্ঠ সনাক্তকরণ এবং লুকানো পৃষ্ঠ অপসারণ: উদাঃ জেড-বাফার (বা, গভীরতা বাফার), গভীরতা-বাছাই, বিএসপি-ট্রি অ্যালগরিদম; স্ক্যান রূপান্তর এবং ক্লিপিং অ্যালগরিদম: উদাঃ কোহেন-সাদারল্যান্ড, সাইরাস-বেক, সাদারল্যান্ড-হজম্যান অ্যালগরিদম; ফ্র্যাক্টালস: উদাঃ কোচ বক্ররেখা, স্নোফ্লেক্স, ড্রাগন বক্ররেখা; রে ট্রেসিং: রে কাস্টিং পদ্ধতি, সরাসরি আলোকসজ্জা, গ্লোবাল আলোকসজ্জা, ছায়া, শেডিং এবং টেক্সচার।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("fabio-oyXis2kALVg-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE3715")
                                 .titleEn("Data Communication")
                                 .titleBn("ডাটা কমিউনিকেশন")
                                 .descriptionEn("Introduction of layered network architecture; Introduction of data communication: physical point to point communication, signal, signal representation and processing, signal to noise ratio; Framing techniques; Frequency response of signals: Fourier integrals, Fourier transforms, time domain and frequency domain concept; representation of noise; Introduction to information theory: entropy, information capacity; Modulation and demodulation: amplitude modulation, frequency and phase Modulation; From analog to digital communication: sampling, Nyquist theorem, quantization, digitization of analog signals; Line coding; Techniques of modulation: pulse modulation, pulse amplitude modulation, pulse width modulation, pulse position modulation, pulse code modulation; Multiplexing techniques: time division multiplexing, frequency division multiplexing techniques.")
                                 .descriptionBn("স্তরযুক্ত নেটওয়ার্ক আর্কিটেকচার প্রবর্তন; ডেটা যোগাযোগের ভূমিকা: শারীরিক পয়েন্ট থেকে পয়েন্ট যোগাযোগ, সংকেত, সংকেত উপস্থাপনা এবং প্রক্রিয়াকরণ, শব্দ অনুপাতের সংকেত; ফ্রেমিং কৌশল; সংকেতগুলির ফ্রিকোয়েন্সি প্রতিক্রিয়া: ফুরিয়ার ইন্টিগ্রালস, ফুরিয়ার ট্রান্সফর্মস, সময় ডোমেন এবং ফ্রিকোয়েন্সি ডোমেন ধারণা; গোলমালের উপস্থাপনা; তথ্য তত্ত্বের ভূমিকা: এনট্রপি, তথ্য ক্ষমতা; মডুলেশন এবং ডিমোডুলেশন: প্রশস্ততা মডুলেশন, ফ্রিকোয়েন্সি এবং ফেজ মড্যুলেশন; এনালগ থেকে ডিজিটাল যোগাযোগ: স্যাম্পলিং, নাইকুইস্ট উপপাদ্য, কোয়ান্টাইজেশন, এনালগ সংকেতগুলির ডিজিটাইজেশন; লাইন কোডিং; মড্যুলেশনের কৌশল: পালস মড্যুলেশন, পালস প্রশস্ততা মডুলেশন, পালস প্রস্থ মডুলেশন, পালস পজিশন মডুলেশন, পালস কোড মডুলেশন; মাল্টিপ্লেক্সিং কৌশল: সময় বিভাগ মাল্টিপ্লেক্সিং, ফ্রিকোয়েন্সি বিভাগ মাল্টিপ্লেক্সিং কৌশল।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("mika-baumeister-1Rvpq7rkyvo-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4759")
                                 .titleEn("Wireless and Cellular Communication")
                                 .titleBn("ওয়্যারলেস এবং সেলুলার যোগাযোগ")
                                 .descriptionEn("Cellular concepts: frequency reuse, handoff strategies, interference and system capacity, grade of service, improving capacity and coverage, call blocking probability; Propagation effects: outdoor propagation models, indoor propagation models, power control, Doppler’s effect, small and large scale fades; Wireless LAN Technology; IEEE 802.11: standard, protocol architecture, physical layer and media access control; Mobile IP;Wireless Application Protocol; IEEE 802.16 Broadband Wireless Access; Brief review of 2nd and 3rd generation wireless: GSM, GPRS, CDMA; LTE, LTE-Advanced, and 5G. Vehicular wireless networks, white spaces, IEEE 802.22 regional area net- works, Bluetooth and Bluetooth Smart, wireless personal area networks, wireless protocols for Internet of Things, ZigBee.")
                                 .descriptionBn("সেলুলার ধারণা: ফ্রিকোয়েন্সি পুনঃব্যবহার, হ্যান্ডঅফ কৌশল, হস্তক্ষেপ এবং সিস্টেমের ক্ষমতা, পরিষেবার গ্রেড, ক্ষমতা এবং কভারেজ উন্নত করা, কল ব্লকিং সম্ভাব্যতা; প্রচার প্রভাব: বহিরঙ্গন প্রচার মডেল, গৃহমধ্যস্থ প্রচার মডেল, শক্তি নিয়ন্ত্রণ, ডপলারের প্রভাব, ছোট এবং বড় স্কেল বিবর্ণ; ওয়্যারলেস ল্যান প্রযুক্তি; আইই 802.11: স্ট্যান্ডার্ড, প্রোটোকল আর্কিটেকচার, শারীরিক স্তর এবং মিডিয়া অ্যাক্সেস নিয়ন্ত্রণ; মোবাইল আইপি; ওয়্যারলেস অ্যাপ্লিকেশন প্রোটোকল; আইই 802.16 ব্রডব্যান্ড ওয়্যারলেস অ্যাক্সেস; দ্বিতীয় এবং তৃতীয় প্রজন্মের ওয়্যারলেসের সংক্ষিপ্ত পর্যালোচনা: জিএসএম, জিপিআরএস, সিডিএমএ; এলটিই, এলটিই-অ্যাডভান্সড এবং 5 জি। যানবাহন ওয়্যারলেস নেটওয়ার্ক, হোয়াইট স্পেস, আইইইই 802.22 আঞ্চলিক অঞ্চল নেট-ওয়ার্কস, ব্লুটুথ এবং ব্লুটুথ স্মার্ট, ওয়্যারলেস ব্যক্তিগত অঞ্চল নেটওয়ার্ক, ইন্টারনেট অফ থিংসের জন্য ওয়্যারলেস প্রোটোকল, জিগবি।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("alina-grubnyak-ZiQkhI7417A-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE479")
                                 .titleEn("Advanced Network Serviced and Management")
                                 .titleBn("উন্নত নেটওয়ার্ক সার্ভিসড এবং ম্যানেজমেন্ট")
                                 .descriptionEn("Application specific protocols: domain name services, electronics mail; World Wide Web and Web caching; Network Management (SNMP), error Reporting Mechanism (ICMP), socket Interfaces, file transfer and remote file access; Multimedia application: RTP, session control; Intra- and Inter-AS routing: IGP, EGP, BGP; Network security: cryptography, firewalls, access control lists (ACLs); VPN, IPSec, IPv6.")
                                 .descriptionBn("অ্যাপ্লিকেশন নির্দিষ্ট প্রোটোকল: ডোমেন নাম সেবা, ইলেকট্রনিক্স মেইল; ওয়ার্ল্ড ওয়াইড ওয়েব এবং ওয়েব ক্যাশিং; নেটওয়ার্ক ম্যানেজমেন্ট (এসএনএমপি), ত্রুটি রিপোর্টিং প্রক্রিয়া (আইসিএমপি), সকেট ইন্টারফেস, ফাইল স্থানান্তর এবং দূরবর্তী ফাইল অ্যাক্সেস; মাল্টিমিডিয়া অ্যাপ্লিকেশন: আরটিপি, সেশন নিয়ন্ত্রণ; ইন্ট্রা- এবং ইন্টার-এএস রাউটিং: আইজিপি, ইজিপি, বিজিপি; নেটওয়ার্ক সুরক্ষা: ক্রিপ্টোগ্রাফি, ফায়ারওয়াল, অ্যাক্সেস কন্ট্রোল তালিকা (এসিএল); ভিপিএন, আইপিসেক, আইপিভি 6।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("rc-xyz-nft-gallery-1C37UztDU8s-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4783")
                                 .titleEn("Cryptography")
                                 .titleBn("ক্রিপ্টোগ্রাফি")
                                 .descriptionEn("Cryptography, history of cryptography; Perfect ciphers, Stream ciphers, attacks on stream ciphers, block ciphers, how to use block ciphers with one time key and many time key; Symmetric encryption, , DES, TDES, AES, Feistel block structure;Asymmetric key: public key protocols, basic key exchange, RSA (cryptosystem); Quantum cryptography, one time pad exchange using qbits; Message integrity (MAC), HMAC, Secure hash functions. Digital signatures.")
                                 .descriptionBn("ক্রিপ্টোগ্রাফি, ক্রিপ্টোগ্রাফির ইতিহাস; পারফেক্ট সাইফার, স্ট্রিম সাইফার, স্ট্রিম সাইফারের উপর আক্রমণ, ব্লক সাইফার, ওয়ান টাইম কী এবং অনেক টাইম কী দিয়ে ব্লক সাইফার কীভাবে ব্যবহার করতে হয়; প্রতিসম এনক্রিপশন, ডিইএস, টিডিইএস, এইএস, ফিস্টেল ব্লক স্ট্রাকচার; অসমমিতিক কী: পাবলিক কী প্রোটোকল, বেসিক কী এক্সচেঞ্জ, আরএসএ (ক্রিপ্টোসিস্টেম); কোয়ান্টাম ক্রিপ্টোগ্রাফি, কিউবিট ব্যবহার করে ওয়ান টাইম প্যাড এক্সচেঞ্জ; বার্তা অখণ্ডতা (ম্যাক), এইচএমএসি, নিরাপদ হ্যাশ ফাংশন। ডিজিটাল স্বাক্ষর।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("philipp-katzenberger-iIJrUoeRoCQ-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4777")
                                 .titleEn("Networks Security")
                                 .titleBn("নেটওয়ার্ক নিরাপত্তা")
                                 .descriptionEn("Introduction to computer security, CIA TRIAD, Threats and Attacks, Passive and Active attacks and examples of passive as well as active attacks, security mechanisms, network security model; Hashing, Cryptography, Introduction to Symmetric key and Asymmetric key encryption; One way authentication protocols, Needham Schroeder protocol, Needham-Schroeder Symmetric key protocol Anomaly in Needham Schroeder Symmetric key protocol, Needham-Schroeder Asymmetric key protocol (Kerberos); IP Sec, Intrusion Detection System (IDS) (Firewall), TLS, HTTPS, TELNET, SSH, Wire-shark; Wireless network security: WEP, WPA, WPA2; Secure Hash Algorithm (SHA), Digital Signature Standard (DSS); Advanced network security topics.")
                                 .descriptionBn("কম্পিউটার সিকিউরিটির ভূমিকা, সিআইএ ট্রায়াড, হুমকি এবং আক্রমণ, প্যাসিভ এবং সক্রিয় আক্রমণ এবং প্যাসিভ পাশাপাশি সক্রিয় আক্রমণের উদাহরণ, সুরক্ষা ব্যবস্থা, নেটওয়ার্ক সুরক্ষা মডেল; হ্যাশিং, ক্রিপ্টোগ্রাফি, ইন্ট্রোডাকশন টু সিমেট্রিক কী এবং অ্যাসিমেট্রিক কী এনক্রিপশন; ওয়ান ওয়ে অথেনটিকেশন প্রোটোকল, নিডহ্যাম শ্রোয়েডার প্রোটোকল, নিডহ্যাম-শ্রোয়েডার সিমেট্রিক কী প্রোটোকল অ্যানোমালি ইন নিডহ্যাম শ্রোয়েডার সিমেট্রিক কী প্রোটোকল, নিডহ্যাম-শ্রোয়েডার অ্যাসিমেট্রিক কী প্রোটোকল (কার্বেরোস); আইপি সেক, ইন্ট্রুশন ডিটেকশন সিস্টেম (আইডিএস) (ফায়ারওয়াল), টিএলএস, এইচটিটিপিএস, টেলনেট, এসএসএইচ, ওয়্যার-শার্ক; ওয়্যারলেস নেটওয়ার্ক সুরক্ষা: WEP, WPA, WPA2; নিরাপদ হ্যাশ অ্যালগরিদম (এসএইচএ), ডিজিটাল স্বাক্ষর স্ট্যান্ডার্ড (ডিএসএস); উন্নত নেটওয়ার্ক নিরাপত্তা বিষয়")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("microsoft-edge-Px0X7g1mc8k-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4763")
                                 .titleEn("Electronic Business")
                                 .titleBn("ইলেকট্রনিক ব্যবসা")
                                 .descriptionEn("The E-Business Framework: difference between electronic business and electronic commerce, electronic mar- kets, disintermediation, horizontal and vertical market places; E-Products and E-Services; Classification of business webs: agora, aggregation, value chain, alliance, supply chain net; business model for e-products and e-services, branding and pricing; E-Procurement: difference between purchase and procurement, market solutions: sell-side, buy-side, and market place; Integration of product catalogue, procurement service providing; Online Marketing: comparison of online media, usage of Internet and websites, stages of a customer development model: surfer, consumer, prosumer, buyer, and key customer; E-Contracting: generic services, information, negotiation, archiving, enforcement, reconciliation, structure of a contract, digital signature, legal affairs; Online Distribution: components of a distribution system, characterisation of online distribution, hybrid distribution networks, model for electronic software distribution; E-Payment: electronic means of payment, micro and macro payment, classification of payment systems, credit cards, customer accounts, digital money; secure transactions; Electronic customer relationship management: objectives of CRM, customer acquisition and liaison, customer buying cycle, architecture of CRM systems, customer satisfaction survey; E-Business environment: information society, building process for communities, multi-option society, ethics in electronic business.")
                                 .descriptionBn("ই-বিজনেস ফ্রেমওয়ার্ক: ইলেকট্রনিক ব্যবসা এবং ইলেকট্রনিক বাণিজ্য, বৈদ্যুতিন মার্কেট, ডিসইন্টারমিডিয়েশন, অনুভূমিক এবং উল্লম্ব বাজারের স্থানগুলির মধ্যে পার্থক্য; ই-পণ্য ও ই-সেবা; ব্যবসায়িক ওয়েবের শ্রেণিবিন্যাস: আগোরা, সমষ্টি, ভ্যালু চেইন, অ্যালায়েন্স, সাপ্লাই চেইন নেট; ই-পণ্য এবং ই-পরিষেবাদি, ব্র্যান্ডিং এবং মূল্য নির্ধারণের জন্য ব্যবসায়িক মডেল; ই-প্রকিউরমেন্ট: ক্রয় ও ক্রয়ের মধ্যে পার্থক্য, বাজার সমাধান: বিক্রয়-পক্ষ, ক্রয়-পক্ষ এবং বাজারের স্থান; পণ্য ক্যাটালগ ইন্টিগ্রেশন, ক্রয় সেবা প্রদান; অনলাইন বিপণন: অনলাইন মিডিয়ার তুলনা, ইন্টারনেট এবং ওয়েবসাইটগুলির ব্যবহার, গ্রাহক বিকাশের মডেলের পর্যায়গুলি: সার্ফার, ভোক্তা, প্রোসুমার, ক্রেতা এবং মূল গ্রাহক; ই-কন্ট্রাক্টিং: জেনেরিক সার্ভিস, তথ্য, আলোচনা, আর্কাইভিং, এনফোর্সমেন্ট, রিকনসিলিয়েশন, কনট্রাক্ট স্ট্রাকচার, ডিজিটাল সিগনেচার, লিগ্যাল অ্যাফেয়ার্স; অনলাইন বিতরণ: একটি বিতরণ সিস্টেমের উপাদান, অনলাইন বিতরণের বৈশিষ্ট্য, হাইব্রিড বিতরণ নেটওয়ার্ক, বৈদ্যুতিন সফ্টওয়্যার বিতরণের মডেল; ই-পেমেন্ট: পেমেন্টের বৈদ্যুতিন মাধ্যম, মাইক্রো এবং ম্যাক্রো পেমেন্ট, পেমেন্ট সিস্টেমের শ্রেণিবিন্যাস, ক্রেডিট কার্ড, গ্রাহক অ্যাকাউন্ট, ডিজিটাল অর্থ; নিরাপদ লেনদেন; ইলেক্ট্রনিক গ্রাহক সম্পর্ক ব্যবস্থাপনা: সিআরএমের উদ্দেশ্য, গ্রাহক অধিগ্রহণ এবং যোগাযোগ, গ্রাহক ক্রয় চক্র, সিআরএম সিস্টেমের আর্কিটেকচার, গ্রাহক সন্তুষ্টি জরিপ; ই-বিজনেস এনভায়রনমেন্ট: ইনফরমেশন সোসাইটি, বিল্ডিং প্রসেস ফর কমিউনিটিস, মাল্টি-অপশন সোসাইটি, ইলেক্ট্রনিক বিজনেসে এথিকস।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("skye-studios-PzkeY0i98io-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4547")
                                 .titleEn("Multimedia Systems Design")
                                 .titleBn("মাল্টিমিডিয়া সিস্টেম ডিজাইন")
                                 .descriptionEn("Organization and structure of modern multimedia systems; text, audio and video encoding; Data compression: lossless and lossy techniques; Multimedia networking: Quality of Service management and multimedia protocols; Streaming multimedia: peer-to-peer, video-on-demand, live streaming; Multimedia storage: data placement and scheduling, caching, and data retrieval; Scheduling algorithms for multimedia within OS; Synchronization schemes: in-band and out-band, synchronization skews and specification; Design of real-world multimedia solution.")
                                 .descriptionBn("আধুনিক মাল্টিমিডিয়া সিস্টেমের সংগঠন এবং কাঠামো; টেক্সট, অডিও এবং ভিডিও এনকোডিং; ডেটা সংকোচন: ক্ষতিহীন এবং ক্ষতিকারক কৌশল; মাল্টিমিডিয়া নেটওয়ার্কিং: পরিষেবা ব্যবস্থাপনা এবং মাল্টিমিডিয়া প্রোটোকলের মান; স্ট্রিমিং মাল্টিমিডিয়া: পিয়ার-টু-পিয়ার, ভিডিও-অন-ডিমান্ড, লাইভ স্ট্রিমিং; মাল্টিমিডিয়া স্টোরেজ: ডেটা প্লেসমেন্ট এবং সময়সূচী, ক্যাশিং এবং ডেটা পুনরুদ্ধার; ওএসের মধ্যে মাল্টিমিডিয়ার জন্য অ্যালগরিদমের সময়সূচী; সিঙ্ক্রোনাইজেশন স্কিম: ইন-ব্যান্ড এবং আউট-ব্যান্ড, সিঙ্ক্রোনাইজেশন স্কিউ এবং স্পেসিফিকেশন; রিয়েল-ওয়ার্ল্ড মাল্টিমিডিয়া সমাধান ডিজাইন।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("shubham-dhage-hQQ_ohVlVVk-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4519")
                                 .titleEn("Distributed Systems")
                                 .titleBn("ডিস্ট্রিবিউটেড সিস্টেম")
                                 .descriptionEn("Remote invocation and indirect communication;Time and coordination; Overlay networks and P2P; Distributed storage and file systems; Name services; Global state and transactions; Replication and consistency; Consensus; Fault tolerance; Security and privacy; Emerging topics in distributed systems.")
                                 .descriptionBn("দূরবর্তী আহ্বান এবং পরোক্ষ যোগাযোগ; সময় ও সমন্বয়; ওভারলে নেটওয়ার্ক এবং পি 2 পি; বিতরণ স্টোরেজ এবং ফাইল সিস্টেম; নাম সেবা; বৈশ্বিক রাষ্ট্র ও লেনদেন; প্রতিলিপি এবং ধারাবাহিকতা; ঐকমত্য; দোষ সহনশীলতা; নিরাপত্তা এবং গোপনীয়তা; বিতরণ সিস্টেমে উদীয়মান বিষয়।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("mohammad-rahmani-gA396xahf-Q-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4523")
                                 .titleEn("Simulation and Modeling")
                                 .titleBn("সিমুলেশন এবং মডেলিং")
                                 .descriptionEn("Simulation methods, model building, random number generator, statistical analysis of results, validation and verification techniques; Digital simulation of continuous system; Simulation and analytical methods for analysis of computer systems and practical problems in business and practice; Introduction to simulation packages.")
                                 .descriptionBn("সিমুলেশন পদ্ধতি, মডেল বিল্ডিং, র্যান্ডম সংখ্যা জেনারেটর, ফলাফলের পরিসংখ্যানগত বিশ্লেষণ, বৈধতা এবং যাচাইকরণ কৌশল; ক্রমাগত সিস্টেমের ডিজিটাল সিমুলেশন; কম্পিউটার সিস্টেম বিশ্লেষণের জন্য সিমুলেশন এবং বিশ্লেষণাত্মক পদ্ধতি এবং ব্যবসা এবং অনুশীলনের ব্যবহারিক সমস্যা; সিমুলেশন প্যাকেজগুলির পরিচিতি।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("growtika-Am6pBe2FpJw-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4587")
                                 .titleEn("Cloud Computing")
                                 .titleBn("ক্লাউড কম্পিউটিং")
                                 .descriptionEn("Basic Concepts: cloud computing and applications, assessing the value proposition, issues and challenges, cloud architecture, service models, deployment models; Cloud Platforms: abstraction and virtualization, capacity planning, platform as a service, Amazon web services , Microsoft Azure, Google cloud platform; Cloud Infrastructure: managing the cloud, cloud security; Services and Applications: service-oriented architecture, moving applications to the cloud, cloud-based storage, media and streaming, cloud based mobile apps and web services.")
                                 .descriptionBn("মৌলিক ধারণা: ক্লাউড কম্পিউটিং এবং অ্যাপ্লিকেশন, মূল্য প্রস্তাব, সমস্যা এবং চ্যালেঞ্জ, ক্লাউড আর্কিটেকচার, পরিষেবা মডেল, স্থাপনার মডেল মূল্যায়ন; ক্লাউড প্ল্যাটফর্ম: বিমূর্ততা এবং ভার্চুয়ালাইজেশন, ক্ষমতা পরিকল্পনা, একটি পরিষেবা হিসাবে প্ল্যাটফর্ম, অ্যামাজন ওয়েব ")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("resource-database-heCbzgivnHA-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4567")
                                 .titleEn("Advanced Database Management Systems")
                                 .titleBn("অ্যাডভান্সড ডাটাবেস ম্যানেজমেন্ট সিস্টেম")
                                 .descriptionEn("Database system architecture; Managing primary and secondary storage; Query processing; Metadata and catalog management; Language processing; Query optimization and plan generation; Concurrency; Failures and recovery; Extensibility; Client-server interactions; Object-oriented database systems, XML, database and the web, data management in distributed mobile computing environment, data broadcasting, text database, digital library design and implementation; Multimedia database: basic concepts, design and optimization of access strategies;Parallel database, spatial database, temporal database; Parallel and distributed database systems; NoSQL; New database architectures and query operators.")
                                 .descriptionBn("ডাটাবেস সিস্টেম আর্কিটেকচার; প্রাথমিক এবং মাধ্যমিক স্টোরেজ পরিচালনা; ক্যোয়ারী প্রসেসিং; মেটাডেটা এবং ক্যাটালগ ম্যানেজমেন্ট; ভাষা প্রক্রিয়াকরণ; ক্যোয়ারী অপ্টিমাইজেশান এবং পরিকল্পনা প্রজন্ম; কনকারেন্সি; ব্যর্থতা এবং পুনরুদ্ধার; এক্সটেনসিবিলিটি; ক্লায়েন্ট-সার্ভার মিথস্ক্রিয়া; অবজেক্ট-ওরিয়েন্টেড ডাটাবেস সিস্টেম, এক্সএমএল, ডাটাবেস এবং ওয়েব, বিতরণ মোবাইল কম্পিউটিং পরিবেশে ডেটা ম্যানেজমেন্ট, ডেটা ব্রডকাস্টিং, টেক্সট ডাটাবেস, ডিজিটাল লাইব্রেরি ডিজাইন এবং বাস্তবায়ন; মাল্টিমিডিয়া ডাটাবেস: অ্যাক্সেস কৌশলগুলির মৌলিক ধারণা, নকশা এবং অপ্টিমাইজেশান; সমান্তরাল ডাটাবেস, স্থানিক ডাটাবেস, অস্থায়ী ডাটাবেস; সমান্তরাল এবং বিতরণ ডাটাবেস সিস্টেম; নোএসকিউএল; নতুন ডাটাবেস আর্কিটেকচার এবং ক্যোয়ারী অপারেটর।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("rishi-WiCvC9u7OpE-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4889")
                                 .titleEn("Machine Learning")
                                 .titleBn("মেশিন লার্নিং")
                                 .descriptionEn("Introduction to Machine Learning; Regression analysis: linear regression; Classification techniques: classification trees, support vector machines; Statistical performance evaluation: bias-variance tradeoff; VC dimension; Reinforcement Learning; Neural networks; EM Algorithm; Unsupervised Learning: k-means clustering; Principal component analysis; Deep Learning; Practical applications of machine learning.")
                                 .descriptionBn("মেশিন লার্নিং পরিচিতি; রিগ্রেশন বিশ্লেষণ: রৈখিক রিগ্রেশন; শ্রেণীবিভাগ কৌশল: শ্রেণীবিভাগ গাছ, সমর্থন ভেক্টর মেশিন; পরিসংখ্যানগত কর্মক্ষমতা মূল্যায়ন: পক্ষপাত-বৈকল্পিক ট্রেডঅফ; ভিসি মাত্রা; রিইনফোর্সমেন্ট লার্নিং; নিউরাল নেটওয়ার্ক; ইএম অ্যালগরিদম; আনসুপারভাইজড লার্নিং: কে-মানে ক্লাস্টারিং; প্রধান উপাদান বিশ্লেষণ; ডিপ লার্নিং; মেশিন লার্নিংয়ের ব্যবহারিক প্রয়োগ।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("rohan-ZoXCoH7tja0-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4891")
                                 .titleEn("Data Mining")
                                 .titleBn("ডাটা মাইনিং")
                                 .descriptionEn("Introduction to data mining: data mining task and applications, data preprocessing, feature selection, association analysis, frequent item-set mining; Single model classifier: k-nearest neighbor, na¨ıve Bayes classifier, decision tree induction, na¨ıve Bayesian tree, rule-based classifiers; Model evaluation and selection; Ensemble learning: random Forests, bagging, boosting, isolated forests; Clustering: k-means clustering, similarity-based clustering, nearest-neighbor clustering, density-based clustering, ensemble clustering, evaluation of clustering methods, clustering high-dimensional data; Data balancing methods; Active learning; Transfer learning; Outlier detection; Concept drift.")
                                 .descriptionBn("ডেটা মাইনিংয়ের ভূমিকা: ডেটা মাইনিং টাস্ক এবং অ্যাপ্লিকেশন, ডেটা প্রিপ্রসেসিং, বৈশিষ্ট্য নির্বাচন, সমিতি বিশ্লেষণ, ঘন ঘন আইটেম-সেট মাইনিং; একক মডেল শ্রেণিবদ্ধকারী: কে-নিকটতম প্রতিবেশী, নাভে বায়েস ক্লাসিফায়ার, সিদ্ধান্ত গাছ আনয়ন, বায়েসিয়ান গাছ, নিয়ম-ভিত্তিক শ্রেণিবদ্ধকারী; মডেল মূল্যায়ন এবং নির্বাচন; এনসেম্বল লার্নিং: এলোমেলো বন, ব্যাগিং, বুস্টিং, বিচ্ছিন্ন বন; ক্লাস্টারিং: কে-মানে ক্লাস্টারিং, সাদৃশ্য-ভিত্তিক ক্লাস্টারিং, নিকটতম প্রতিবেশী ক্লাস্টারিং, ঘনত্ব-ভিত্তিক ক্লাস্টারিং, এনসেম্বল ক্লাস্টারিং, ক্লাস্টারিং পদ্ধতির মূল্যায়ন, উচ্চ-মাত্রিক ডেটা ক্লাস্টারিং; ডেটা ব্যালেন্সিং পদ্ধতি; সক্রিয় শিক্ষা; ট্রান্সফার লার্নিং; বহিরাগত সনাক্তকরণ; কনসেপ্ট ড্রিফট।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("digitale-de-2GEr4fLZt8A-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4893")
                                 .titleEn("Introduction to Bioinformatics")
                                 .titleBn("বায়োইনফরমেটিক্স পরিচিতি")
                                 .descriptionEn("Introduction; Molecular biology basics: DNA, RNA, genes, and proteins; Graph algorithms: DNA sequencing, DNA fragment assembly, Spectrum graphs; Sequence similarity; Suffix Tree and variants with applications; Genome Alignment: maximum unique match, LCS, mutation sensitive alignments; Database search: Smith-Waterman algorithm, FASTA, BLAST and its variations; Locality sensitive hashing; Multiple sequence alignment; Phylogeny reconstruction; Phylogeny comparison: similarity and dissimilarity measurements, consensus tree problem; Genome rearrangement: types of genome rearrangements, sorting by reversal and other operations; Motif finding; RNA secondary structure prediction; Peptide sequencing; Population genetics; Recent Trends in Bioinformatics.")
                                 .descriptionBn("ভূমিকা; আণবিক জীববিজ্ঞানের মূল বিষয়: ডিএনএ, আরএনএ, জিন এবং প্রোটিন; গ্রাফ অ্যালগরিদম: ডিএনএ সিকোয়েন্সিং, ডিএনএ ফ্র্যাগমেন্ট অ্যাসেম্বলি, স্পেকট্রাম গ্রাফ; ক্রম সাদৃশ্য; প্রত্যয় গাছ এবং অ্যাপ্লিকেশন সঙ্গে বৈকল্পিক; জিনোম প্রান্তিককরণ: সর্বাধিক অনন্য ম্যাচ, এলসিএস, মিউটেশন সংবেদনশীল প্রান্তিককরণ; ডাটাবেস অনুসন্ধান: স্মিথ-ওয়াটারম্যান অ্যালগরিদম, ফাস্টা, ব্লাস্ট এবং এর বিভিন্নতা; স্থানীয় সংবেদনশীল হ্যাশিং; একাধিক ক্রম প্রান্তিককরণ; ফাইলোজিনি পুনর্গঠন; ফিলোজিনি তুলনা: সাদৃশ্য এবং অমিল পরিমাপ, ঐক্যমত্য গাছ সমস্যা; জিনোম পুনর্বিন্যাস: জিনোম পুনর্বিন্যাসের ধরণ, বিপরীত এবং অন্যান্য ক্রিয়াকলাপ দ্বারা বাছাই; মোটিফ খোঁজা; আরএনএ সেকেন্ডারি স্ট্রাকচার ভবিষ্যদ্বাণী; পেপটাইড সিকোয়েন্সিং; জনসংখ্যা জেনেটিক্স; বায়োইনফরম্যাটিক্সের সাম্প্রতিক প্রবণতা.")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("headway-5QgIuuBxKwM-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4883 ")
                                 .titleEn("Digital Image Processing")
                                 .titleBn("ডিজিটাল ইমেজ প্রসেসিং")
                                 .descriptionEn("Digital Image Fundamentals: visual perception, sensing, acquisition, sampling, quantization; Intensity Trans- formation and Spatial Filtering: different transformations, histogram, correlation and convolution, smoothing and sharpening filters; Filtering in Frequency Domain: discrete fourier transformation (DFT) of image, smoothing and sharpening in frequency domain, selective filtering; Image Restoration and Reconstruction: noise models, spatial filtering for noise, frequency filtering for noise, reconstruction from projections; Color Image Processing: color models, color transformation and segmentation; Morphological Image Processing: erosion, dilation, opening, closing, morphological algorithms; Image Compression: redundancy, fidelity criteria, some basic compression techniques; Image Segmentation: point, line and edge detection, thresholding, region based segmentation; Object Recognition: matching, statistical classifier, neural networks.")
                                 .descriptionBn("ডিজিটাল ইমেজ ফান্ডামেন্টালস: ভিজ্যুয়াল উপলব্ধি, সেন্সিং, অধিগ্রহণ, নমুনা, কোয়ান্টাইজেশন; তীব্রতা ট্রান্স-ফর্মেশন এবং স্থানিক ফিল্টারিং: বিভিন্ন রূপান্তর, হিস্টোগ্রাম, পারস্পরিক সম্পর্ক এবং কনভোলিউশন, মসৃণ এবং তীক্ষ্ণ ফিল্টার; ফ্রিকোয়েন্সি ডোমেনে ফিল্টারিং: চিত্রের বিচ্ছিন্ন ফুরিয়ার রূপান্তর (ডিএফটি), ফ্রিকোয়েন্সি ডোমেনে মসৃণ এবং তীক্ষ্ণকরণ, নির্বাচনী ফিল্টারিং; চিত্র পুনরুদ্ধার এবং পুনর্গঠন: শব্দ মডেল, শব্দের জন্য স্থানিক ফিল্টারিং, গোলমালের জন্য ফ্রিকোয়েন্সি ফিল্টারিং, অনুমান থেকে পুনর্গঠন; রঙ চিত্র প্রক্রিয়াকরণ: রঙ মডেল, রঙ রূপান্তর এবং বিভাজন; মরফোলজিকাল ইমেজ প্রসেসিং: ক্ষয়, প্রসারণ, খোলার, বন্ধ, রূপচর্চা অ্যালগরিদম; চিত্র সংক্ষেপণ: অপ্রয়োজনীয়তা, বিশ্বস্ততার মানদণ্ড, কিছু মৌলিক সংকোচন কৌশল; চিত্র বিভাজন: বিন্দু, লাইন এবং প্রান্ত সনাক্তকরণ, থ্রেশহোল্ডিং, অঞ্চল ভিত্তিক বিভাজন; অবজেক্ট রিকগনিশন: ম্যাচিং, স্ট্যাটিস্টিকাল ক্লাসিফায়ার, নিউরাল নেটওয়ার্ক।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("carlos-muza-hpjSkU2UYSU-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4817")
                                 .titleEn("Big Data Analytics")
                                 .titleBn("বিগ ডেটা অ্যানালিটিক্স")
                                 .descriptionEn("Introduction to Big Data: characteristics of Big Data and dimensions of scalability; Data Science: getting value out of Big Data, foundations for Big Data systems and programming, getting started with Hadoop; Big Data Modelling and Management Systems: Big Data modelling, Big Data management, designing a Big Data management system; Big Data Integration and Processing: retrieving Big Data, Big Data integration, processing Big Data, Big Data analytics using Spark; Machine Learning with Big Data: introduction to machine learning with Big Data, data exploration, classification, evaluation of machine learning models, regression, cluster analysis, and association analysis; Graph Analytics for Big Data: introduction to graphs, graph Analytics, graph analytics techniques, computing platforms for graph analytics.")
                                 .descriptionBn("বিগ ডেটা পরিচিতি: বিগ ডেটার বৈশিষ্ট্য এবং স্কেলাবিলিটির মাত্রা; ডেটা সায়েন্স: বিগ ডেটা থেকে মূল্য পাওয়া, বিগ ডেটা সিস্টেম এবং প্রোগ্রামিংয়ের ভিত্তি, হ্যাডোপ দিয়ে শুরু করা; বিগ ডেটা মডেলিং এবং ম্যানেজমেন্ট সিস্টেম: বিগ ডেটা মডেলিং, বিগ ডেটা ম্যানেজমেন্ট, একটি বিগ ডেটা ম্যানেজমেন্ট সিস্টেম ডিজাইন; বিগ ডেটা ইন্টিগ্রেশন এবং প্রসেসিং: বিগ ডেটা পুনরুদ্ধার, বিগ ডেটা ইন্টিগ্রেশন, বিগ ডেটা প্রসেসিং, স্পার্ক ব্যবহার করে বিগ ডেটা অ্যানালিটিক্স; বিগ ডেটা সহ মেশিন লার্নিং: বিগ ডেটার সাথে মেশিন লার্নিংয়ের ভূমিকা, ডেটা এক্সপ্লোরেশন, শ্রেণিবিন্যাস, মেশিন লার্নিং মডেলগুলির মূল্যায়ন, রিগ্রেশন, ক্লাস্টার বিশ্লেষণ এবং সমিতি বিশ্লেষণ; বিগ ডেটার জন্য গ্রাফ অ্যানালিটিক্স: গ্রাফের ভূমিকা, গ্রাফ অ্যানালিটিক্স, গ্রাফ বিশ্লেষণ কৌশল, গ্রাফ বিশ্লেষণের জন্য কম্পিউটিং প্ল্যাটফর্ম।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("andrew-neel-QLqNalPe0RA-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4451")
                                 .titleEn("Human Computer Interaction")
                                 .titleBn("মানব কম্পিউটার মিথস্ক্রিয়া")
                                 .descriptionEn("Foundations of human computer interaction: understanding and conceptualizing interaction; Understanding users: human perception, ergonomics, cognition, psychology; Task Analysis; User Interface Design, interface programming, graphical user interfaces, user survey, user journey and experience, mobile devices, multimodal interfaces and ubiquitous computing, user-centered system development and evaluation, user- centered software development and evaluation; Prototyping; Interaction design for new environments; Affective and social computing; Assistive and augmentative communication, assistive technology and rehabilitation; Human machine interface, brain computer interface; Experimental research ethics.")
                                 .descriptionBn("মানব কম্পিউটার মিথস্ক্রিয়া ভিত্তি: মিথস্ক্রিয়া বোঝা এবং ধারণা; ব্যবহারকারীদের বোঝা: মানব উপলব্ধি, এরগনোমিক্স, জ্ঞান, মনোবিজ্ঞান; টাস্ক বিশ্লেষণ; ইউজার ইন্টারফেস ডিজাইন, ইন্টারফেস প্রোগ্রামিং, গ্রাফিক্যাল ইউজার ইন্টারফেস, ইউজার সার্ভে, ইউজার জার্নি অ্যান্ড এক্সপেরিয়েন্স, মোবাইল ডিভাইস, মাল্টিমোডাল ইন্টারফেস এবং সর্বব্যাপী কম্পিউটিং, ইউজার-কেন্দ্রিক সিস্টেম ডেভেলপমেন্ট অ্যান্ড ইভালুয়েশন, ইউজার কেন্দ্রিক সফটওয়্যার ডেভেলপমেন্ট অ্যান্ড ইভালুয়েশন; প্রোটোটাইপিং; নতুন পরিবেশের জন্য মিথস্ক্রিয়া নকশা; সংবেদনশীল এবং সামাজিক কম্পিউটিং; সহায়ক এবং বর্ধিত যোগাযোগ, সহায়ক প্রযুক্তি এবং পুনর্বাসন; হিউম্যান মেশিন ইন্টারফেস, ব্রেইন কম্পিউটার ইন্টারফেস; পরীক্ষামূলক গবেষণা নীতিশাস্ত্র.")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("thisisengineering-mvbtVeRVJzg-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4435 ")
                                 .titleEn("Software Architecture")
                                 .titleBn("সফটওয়্যার আর্কিটেকচার")
                                 .descriptionEn("Introduction; Design vs Architecture; Enterprise Architecture; Architectural drivers; Software Architecture role; Skills and knowledge of software architect; Software architecture in the delivery process; Visualizing Software Architecture; Managing risks; Architectural recovery, architectural styles, domain specific software architectures coupled with programming/implementation effort, design and implement a real-world software system, the state- of-the-art in software architecture research and future trends.")
                                 .descriptionBn("ভূমিকা; ডিজাইন বনাম আর্কিটেকচার; এন্টারপ্রাইজ আর্কিটেকচার; আর্কিটেকচারাল ড্রাইভার; সফটওয়্যার আর্কিটেকচার ভূমিকা; সফটওয়্যার আর্কিটেক্টের দক্ষতা ও জ্ঞান; ডেলিভারি প্রক্রিয়াতে সফ্টওয়্যার আর্কিটেকচার; সফটওয়্যার আর্কিটেকচার ভিজ্যুয়ালাইজ; ঝুঁকি ব্যবস্থাপনা; স্থাপত্য পুনরুদ্ধার, স্থাপত্য শৈলী, ডোমেন নির্দিষ্ট সফ্টওয়্যার আর্কিটেকচার প্রোগ্রামিং / বাস্তবায়ন প্রচেষ্টার সাথে মিলিত, একটি বাস্তব-বিশ্বের সফ্টওয়্যার সিস্টেম ডিজাইন এবং বাস্তবায়ন, সফ্টওয়্যার আর্কিটেকচার গবেষণা এবং ভবিষ্যতের প্রবণতাগুলিতে অত্যাধুনিক")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("thisisengineering-Bg0Geue-cY8-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4495")
                                 .titleEn("Software Testing and Quality Assurance")
                                 .titleBn("সফ্টওয়্যার টেস্টিং এবং গুণমান নিশ্চিতকরণ")
                                 .descriptionEn("Testing strategies: SDLC vs STLC; Testing Levels; Testing methods; Testing types: Specification-based vs. code-based, black-box vs. white-box, functional vs. structural testing; unit, integration, system, acceptance, and regression testing; Load, Performance, Stress, Unit Testing; Verification vs. validation; Test planning: scenario, case, traceability matrix; ISO Standards; Agile testing; Testing Estimation techniques; Introduction to software reliability, quality control and quality assurance; Formal verification methods; static and dynamic program verification.")
                                 .descriptionBn("পরীক্ষার কৌশল: এসডিএলসি বনাম এসটিএলসি; পরীক্ষার মাত্রা; পরীক্ষা পদ্ধতি; পরীক্ষার ধরণ: স্পেসিফিকেশন-ভিত্তিক বনাম কোড-ভিত্তিক, ব্ল্যাক-বক্স বনাম হোয়াইট-বক্স, কার্যকরী বনাম কাঠামোগত পরীক্ষা; ইউনিট, ইন্টিগ্রেশন, সিস্টেম, গ্রহণযোগ্যতা এবং রিগ্রেশন পরীক্ষা; লোড, কর্মক্ষমতা, চাপ, ইউনিট পরীক্ষা; যাচাইকরণ বনাম বৈধতা; পরীক্ষার পরিকল্পনা: পরিস্থিতি, কেস, ট্রেসেবিলিটি ম্যাট্রিক্স; আইএসও স্ট্যান্ডার্ড; চটপটে পরীক্ষা; অনুমান কৌশল পরীক্ষা; সফ্টওয়্যার নির্ভরযোগ্যতা, মান নিয়ন্ত্রণ এবং গুণমান নিশ্চিতকরণের ভূমিকা; আনুষ্ঠানিক যাচাইকরণ পদ্ধতি; স্ট্যাটিক এবং গতিশীল প্রোগ্রাম যাচাইকরণ।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("nordwood-themes-ubIWo074QlU-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4485")
                                 .titleEn("Game Design and Development")
                                 .titleBn("গেম ডিজাইন এবং ডেভেলপমেন্ট")
                                 .descriptionEn("Introduction to games: history, games and society; Game design: design concepts, teams and processes character modelling, animation, storyline, programming fundamentals, concepts of 3D virtual world; Game Engines: 3D mesh and object modelling, simulation and collision detection, etc; Debugging games; Game Architecture; Memory and I/O systems; Development of a customized game; Advanced Topics: data structures , AI, etc in Games; Networks and multiplayer mode; Application of Games: simulation, animation movies and others.")
                                 .descriptionBn("গেমসের ভূমিকা: ইতিহাস, গেমস এবং সমাজ; গেম ডিজাইন: ডিজাইন ধারণা, দল এবং প্রক্রিয়া চরিত্র মডেলিং, অ্যানিমেশন, কাহিনী, প্রোগ্রামিং মৌলিক বিষয়, 3 ডি ভার্চুয়াল বিশ্বের ধারণা; গেম ইঞ্জিন: 3 ডি জাল এবং অবজেক্ট মডেলিং, সিমুলেশন এবং সংঘর্ষ সনাক্তকরণ ইত্যাদি; ডিবাগিং গেমস; খেলা আর্কিটেকচার; মেমরি এবং আই / ও সিস্টেম; একটি কাস্টমাইজড খেলা উন্নয়ন; উন্নত বিষয়: গেমগুলিতে ডেটা স্ট্রাকচার, এআই ইত্যাদি; নেটওয়ার্ক এবং মাল্টিপ্লেয়ার মোড; গেম অ্যাপ্লিকেশন: সিমুলেশন, অ্যানিমেশন চলচ্চিত্র এবং অন্যান্য।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("lee-campbell-DtDlVpy-vvQ-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4329")
                                 .titleEn("Digital System Design")
                                 .titleBn("ডিজিটাল সিস্টেম ডিজাইন")
                                 .descriptionEn("Design using MSI and LSI components; Programmable logic devices; Basic components of a computer system; Design of processing unit: ALU, Comparator, Accumulator, Shifter, Multiplier; Hardware multiplication: Booth and Modified Booth algorithm; Design of control unit: hardwired and microprogrammed; Simple-As-Possible (SAP) computer: SAP-1, selected concepts from SAP-2 and SAP-3 (jump, call, return, stack, push and pop); Designing microprocessor based system; Hardware Interfacing with Intel 8086 microprocessor: programmable peripheral interface, programmable interrupt controller, programmable timer, keyboard and display interface.")
                                 .descriptionBn("এমএসআই এবং এলএসআই উপাদান ব্যবহার করে ডিজাইন; প্রোগ্রামেবল লজিক ডিভাইস; একটি কম্পিউটার সিস্টেমের মৌলিক উপাদান; প্রসেসিং ইউনিটের নকশা: এএলইউ, তুলনাকারী, অ্যাকুমুলেটর, শিফটার, গুণক; হার্ডওয়্যার গুণ: বুথ এবং পরিবর্তিত বুথ অ্যালগরিদম; কন্ট্রোল ইউনিট ডিজাইন: হার্ডওয়্যারড এবং মাইক্রোপ্রোগ্রামড; সিম্পল-এজ-পসিবল (এসএপি) কম্পিউটার: এসএপি -১, এসএপি -২ এবং এসএপি -৩ থেকে নির্বাচিত ধারণাগুলি (জাম্প, কল, রিটার্ন, স্ট্যাক, পুশ এবং পপ); মাইক্রোপ্রসেসর ভিত্তিক সিস্টেম ডিজাইন করা; ইন্টেল 8086 মাইক্রোপ্রসেসরের সাথে হার্ডওয়্যার ইন্টারফেসিং: প্রোগ্রামেবল পেরিফেরাল ইন্টারফেস, প্রোগ্রামেবল ইন্টারাপ্ট কন্ট্রোলার, প্রোগ্রামেবল টাইমার, কীবোর্ড এবং ডিসপ্লে ইন্টারফেস।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("aron-visuals-BXOXnQ26B7o-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4379")
                                 .titleEn("Real-time Embedded Systems")
                                 .titleBn("রিয়েল-টাইম এমবেডেড সিস্টেম")
                                 .descriptionEn("Embedded architectures: 16/32/64-bit embedded processors; Interaction with devices: buses, memory architectures, memory management, device drivers; Concurrency: software and hardware interrupts, timers; Real- time principles: synchronization, scheduling, multi-tasking; Real-time task scheduling: scheduleablity analysis, rate and deadline monotonic scheduling, fixed and dynamic priority scheduling; Feed-back control theory and application; Profiling and code optimization; Embedded software systems: exception handling, loading, mode-switching, programming embedded systems.")
                                 .descriptionBn("এমবেডেড আর্কিটেকচার: 16/32/64-বিট এমবেডেড প্রসেসর; ডিভাইসের সাথে মিথস্ক্রিয়া: বাস, মেমরি আর্কিটেকচার, মেমরি ম্যানেজমেন্ট, ডিভাইস ড্রাইভার; কনকারেন্সি: সফ্টওয়্যার এবং হার্ডওয়্যার বাধা, টাইমার; রিয়েল-টাইম নীতি: সিঙ্ক্রোনাইজেশন, সময়সূচী, মাল্টি-টাস্কিং; রিয়েল-টাইম টাস্ক শিডিয়ুলিং: সময়সূচী বিশ্লেষণ, হার এবং সময়সীমা মনোটোনিক সময়সূচী, স্থির এবং গতিশীল অগ্রাধিকার সময়সূচী; ফিড-ব্যাক নিয়ন্ত্রণ তত্ত্ব এবং প্রয়োগ; প্রোফাইলিং এবং কোড অপ্টিমাইজেশান; এমবেডেড সফ্টওয়্যার সিস্টেম: ব্যতিক্রম হ্যান্ডলিং, লোডিং, মোড-স্যুইচিং, প্রোগ্রামিং এমবেডেড সিস্টেম।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("firmbee-com-gcsNOsPEXfs-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4327")
                                 .titleEn("VLSI Design")
                                 .titleBn("ভিএলএসআই ডিজাইন")
                                 .descriptionEn("VLSI technology: Top down design approach, technology trends and design styles. Review of MOS transistor theory: Threshold voltage, body effect, I-V equations and characteristics, latch-up problems, NMOS inverter, CMOS inverter, pass-transistor and transmission gates. CMOS circuit characteristics and performance estimation: Resistance, capacitance, rise and fall times, delay, gate transistor sizing and power consumption. CMOS circuit and logic design: Layout design rules and physical design of simple logic gates. CMOS subsystem design: Adders, multiplier and memory system, arithmetic logic unit. Programmable logic arrays. I/O systems. VLSI testing.")
                                 .descriptionBn("ভিএলএসআই প্রযুক্তি: শীর্ষ ডাউন নকশা পদ্ধতির, প্রযুক্তি প্রবণতা এবং নকশা শৈলী। এমওএস ট্রানজিস্টর তত্ত্বের পর্যালোচনা: থ্রেশহোল্ড ভোল্টেজ, বডি এফেক্ট, আই-ভি সমীকরণ এবং বৈশিষ্ট্য, ল্যাচ-আপ সমস্যা, এনএমওএস বৈদ্যুতিন সংকেতের মেরু বদল, সিএমওএস বৈদ্যুতিন সংকেতের মেরু বদল, পাস-ট্রানজিস্টর এবং ট্রান্সমিশন গেট। সিএমওএস সার্কিট বৈশিষ্ট্য এবং পারফরম্যান্স অনুমান: প্রতিরোধের, ক্যাপাসিট্যান্স, উত্থান এবং পতনের সময়, বিলম্ব, গেট ট্রানজিস্টর আকার এবং বিদ্যুত খরচ। সিএমওএস সার্কিট এবং লজিক ডিজাইন: লেআউট ডিজাইনের নিয়ম এবং সাধারণ লজিক গেটগুলির শারীরিক নকশা। সিএমওএস সাবসিস্টেম ডিজাইন: অ্যাডার্স, গুণক এবং মেমরি সিস্টেম, গাণিতিক লজিক ইউনিট। প্রোগ্রামেবল লজিক অ্যারে। আই / ও সিস্টেম। ভিএলএসআই পরীক্ষা।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("mohamed-nohassi-9Ge8ngH6JeQ-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4337")
                                 .titleEn("Robotics")
                                 .titleBn("রোবোটিক্স")
                                 .descriptionEn("Introduce the basic concepts of robotics, types of robots, robotics and AI; Automation & autonomy architectures; Robot hardware: sensors, actuators; Robotic mapping: localization, Monte Carlo localization, multi-object localization; Robotic navigation and locomotion: motion planning, dynamics and control; Human-robot interaction: Natural language learning; Multi-agents: tasks and teams.")
                                 .descriptionBn("রোবটিক্সের প্রাথমিক ধারণা, রোবটের প্রকারভেদ, রোবোটিক্স এবং এআই পরিচয় করিয়ে দিন; অটোমেশন এবং স্বায়ত্তশাসন আর্কিটেকচার; রোবট হার্ডওয়্যার: সেন্সর, অ্যাকচুয়েটর; রোবোটিক ম্যাপিং: স্থানীয়করণ, মন্টি কার্লো স্থানীয়করণ, মাল্টি-অবজেক্ট স্থানীয়করণ; রোবোটিক নেভিগেশন এবং লোকোমোশন: গতি পরিকল্পনা, গতিবিদ্যা এবং নিয়ন্ত্রণ; মানব-রোবট মিথস্ক্রিয়া: প্রাকৃতিক ভাষা শেখা; মাল্টি-এজেন্ট: কাজ এবং দল।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("justin-morgan-I3jsaLiK_sc-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4397")
                                 .titleEn("Interfacing")
                                 .titleBn("ইন্টারফেসিং")
                                 .descriptionEn("Definition of interface, types of interfaces; Interface levels; Typical interface mechanisms; Example interfaces; In- put/output ports: I/O port structure, status and control data registers, bidirectional pin operation, bus connection; Three-state output, Z state; Technological considerations; Connections to external loads; Input device connections; Signal multiplexing; Analog Interfaces; Timing and frequency aspects of analogue signals; Nyquist-Shannon sampling theorem; Analog-digital converters; Digital-analog converters; Example application; Serial communication interfaces; Types and characteristics of communication interfaces; Synchronous serial interface (SSI). Real examples (RS232, SPI); Common computer Interfaces; Universal Serial Bus (USB), USB3, Thunderbolt PCI express (PCIe), Storage interfaces – SATA, NVMe, eMMC; Display interfaces – VGA, DVI, Display Port; Microcontroller interfacing – Arduino, Raspberry pi GPIO,; Buses and DMA; Design and operation of interface between computer and the outside world; Human computer interaction, brain Computer interfaces.")
                                 .descriptionBn("ইন্টারফেসের সংজ্ঞা, ইন্টারফেসের প্রকারভেদ; ইন্টারফেস স্তর; সাধারণ ইন্টারফেস প্রক্রিয়া; উদাহরণ ইন্টারফেস; ইন-পুট / আউটপুট পোর্ট: আই / ও পোর্ট কাঠামো, স্থিতি এবং নিয়ন্ত্রণ ডেটা রেজিস্টার, দ্বিমুখী পিন অপারেশন, বাস সংযোগ; তিন-রাষ্ট্র আউটপুট, জেড রাজ্য; প্রযুক্তিগত বিবেচনা; বহিরাগত লোড সংযোগ; ইনপুট ডিভাইস সংযোগ; সিগন্যাল মাল্টিপ্লেক্সিং; এনালগ ইন্টারফেস; এনালগ সংকেতগুলির সময় এবং ফ্রিকোয়েন্সি দিকগুলি; নাইকুইস্ট-শ্যানন স্যাম্পলিং উপপাদ্য; এনালগ-ডিজিটাল রূপান্তরকারী; ডিজিটাল-এনালগ রূপান্তরকারী; উদাহরণ অ্যাপ্লিকেশন; সিরিয়াল কমিউনিকেশন ইন্টারফেস; যোগাযোগ ইন্টারফেসের ধরন এবং বৈশিষ্ট্য; সিঙ্ক্রোনাস সিরিয়াল ইন্টারফেস (এসএসআই)। বাস্তব উদাহরণ (আরএস 232, এসপিআই); সাধারণ কম্পিউটার ইন্টারফেস; ইউনিভার্সাল সিরিয়াল বাস (ইউএসবি), ইউএসবি 3, থান্ডারবোল্ট পিসিআই এক্সপ্রেস (পিসিআই), স্টোরেজ ইন্টারফেস - সাটা, এনভিএমই, ইএমএমসি; ডিসপ্লে ইন্টারফেস - ভিজিএ, ডিভিআই, ডিসপ্লে পোর্ট; মাইক্রোকন্ট্রোলার ইন্টারফেসিং - আরডুইনো, রাস্পবেরি পাই জিপিআইও,; বাস এবং ডিএমএ; কম্পিউটার এবং বাইরের বিশ্বের মধ্যে ইন্টারফেসের নকশা এবং অপারেশন; হিউম্যান কম্পিউটার ইন্টারঅ্যাকশন, ব্রেইন কম্পিউটার ইন্টারফেস।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("isaac-smith-6EnTPvPPL6I-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4941")
                                 .titleEn("Enterprise Systems: Concepts and Practice")
                                 .titleBn("এন্টারপ্রাইজ সিস্টেম: ধারণা এবং অনুশীলন")
                                 .descriptionEn("Materials management (MM); Supply chain management (SCM); Customer relationship management (CRM); Financials, mobile and cloud enterprise systems; Internet-of-Things (IoT) and enterprise BIG data;\n" + "\n" + "The course will incorporate a hands-on component using SAP, Oracle ES software. The course will also incorporate modelling techniques and tools, assess an organisation’s readiness for ES implementation.")
                                 .descriptionBn("উপকরণ ব্যবস্থাপনা (এমএম); সাপ্লাই চেইন ম্যানেজমেন্ট (এসসিএম); গ্রাহক সম্পর্ক ব্যবস্থাপনা (সিআরএম); আর্থিক, মোবাইল এবং ক্লাউড এন্টারপ্রাইজ সিস্টেম; ইন্টারনেট অব থিংস (আইওটি) এবং এন্টারপ্রাইজ বিগ ডেটা;\n" + "\n" + "কোর্সটিতে এসএপি, ওরাকল ইএস সফটওয়্যার ব্যবহার করে একটি হ্যান্ড-অন কম্পোনেন্ট অন্তর্ভুক্ত করা হবে। কোর্সটি মডেলিং কৌশল এবং সরঞ্জামগুলিও অন্তর্ভুক্ত করবে, ইএস বাস্তবায়নের জন্য কোনও সংস্থার প্রস্তুতির মূল্যায়ন করবে।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("domenico-loia-EhTcC9sYXsw-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4943")
                                 .titleEn("Web Application Security")
                                 .titleBn("ওয়েব অ্যাপ্লিকেশন সিকিউরিটি")
                                 .descriptionEn("Client-side (browser) security: vulnerabilities associated with browsing the web, system penetration, information breach and identity threat; Securing the communication channel: encrypting data stream using SSL, confidentiality and integrity of data using third party transaction protocols e.g. SET, PCI DSS standard, the latest evolutions for HTTPS deployments; Securing untrusted data: server-side and client-side injection attacks, defending common injection attacks; Session management and access control: relationship between authentication, authorization and session management, prevent authorization bypasses and harden session management mechanisms; Server-side security: CGI security, server configuration, access control, operating system security, malicious e-mails, web scripts, cookies, web bugs spyware, rogue AV etc.")
                                 .descriptionBn("ক্লায়েন্ট-সাইড (ব্রাউজার) নিরাপত্তা: ওয়েব ব্রাউজিং, সিস্টেম অনুপ্রবেশ, তথ্য লঙ্ঘন এবং পরিচয় হুমকির সাথে সম্পর্কিত দুর্বলতা; যোগাযোগ চ্যানেল সুরক্ষিত করা: এসএসএল ব্যবহার করে ডেটা স্ট্রিম এনক্রিপ্ট করা, তৃতীয় পক্ষের লেনদেন প্রোটোকল যেমন সেট, পিসিআই ডিএসএস স্ট্যান্ডার্ড, এইচটিটিপিএস স্থাপনার জন্য সর্বশেষ বিবর্তন ব্যবহার করে ডেটা স্ট্রিম এনক্রিপ্ট করা; অবিশ্বস্ত ডেটা সুরক্ষিত করা: সার্ভার-সাইড এবং ক্লায়েন্ট-সাইড ইনজেকশন আক্রমণ, সাধারণ ইনজেকশন আক্রমণগুলি রক্ষা করা; সেশন ম্যানেজমেন্ট এবং অ্যাক্সেস কন্ট্রোল: প্রমাণীকরণ, অনুমোদন এবং সেশন ম্যানেজমেন্টের মধ্যে সম্পর্ক, অনুমোদন বাইপাস প্রতিরোধ এবং সেশন ম্যানেজমেন্ট প্রক্রিয়া কঠোর; সার্ভার-সাইড সিকিউরিটি: সিজিআই সিকিউরিটি, সার্ভার কনফিগারেশন, অ্যাক্সেস কন্ট্রোল, অপারেটিং সিস্টেম সিকিউরিটি, ম্যালিসিয়াস ই-মেইল, ওয়েব স্ক্রিপ্ট, কুকিজ, ওয়েব বাগ স্পাইওয়্যার, দুর্বৃত্ত এভি ইত্যাদি।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("balazs-ketyi-_x335IZXxfc-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4945")
                                 .titleEn("UI Concepts and Design")
                                 .titleBn("UI ধারণা এবং ডিজাইন")
                                 .descriptionEn("Design principles: color, emphasis, usability, hierarchy, etc; Low fidelity wireframes: beginning to design using low fidelity wireframes and storyboards; Introduction to Sketch software, rapid prototyping using Sketch, high fidelity mobile, application, and website wireframes; Creating a style guide with Sketch; Design research and personas: UX fundamentals; find, build, verify, patterns, personas, situations, buy-in, knowledge, scenarios; Using sketch to prototype using material design; Prototype employee time clock; Prototype tablet ordering interface; Prototype iOS todo app; Core principles of design: good, bad and ugly web search; Introduction to Illustrator, introduction to value: understanding Illustrator and designing in Illustrator, refactor and embellish, introduction to color with value, add hue to value; Introduction to PhotoShop, unity in design, PhotoShop and a UI tool, freeform of painting; Introdcution to HTML and Visual Studio Code, learn markup language, tags and structure; Introduction to CSS, design guidelines, and styling; Styling with CSS. Complete content from CSS from scratch; Create new CSS on existing HTML; Basic site and app development in Bootstrap, develop a responsive site that will work on PCs tables and Phones; Basic site and App design in Bootstrap; Design graphics for the responsive site in the previous website.")
                                 .descriptionBn("নকশা নীতি: রঙ, জোর, ব্যবহারযোগ্যতা, শ্রেণিবিন্যাস, ইত্যাদি; - কম বিশ্বস্ততা ওয়্যারফ্রেম: কম বিশ্বস্ততা ওয়্যারফ্রেম এবং স্টোরিবোর্ড ব্যবহার করে ডিজাইন শুরু করা; স্কেচ সফ্টওয়্যার পরিচিতি, স্কেচ ব্যবহার করে দ্রুত প্রোটোটাইপিং, উচ্চ বিশ্বস্ততা মোবাইল, অ্যাপ্লিকেশন এবং ওয়েবসাইট ওয়্যারফ্রেম; স্কেচ দিয়ে একটি স্টাইল গাইড তৈরি করা; ডিজাইন গবেষণা এবং ব্যক্তিত্ব: ইউএক্স ফান্ডামেন্টালস; সন্ধান করুন, তৈরি করুন, যাচাই করুন, নিদর্শন, ব্যক্তিত্ব, পরিস্থিতি, ক্রয়-ইন, জ্ঞান, দৃশ্যকল্প; উপাদান নকশা ব্যবহার করে প্রোটোটাইপ স্কেচ ব্যবহার; প্রোটোটাইপ কর্মচারী সময় ঘড়ি; প্রোটোটাইপ ট্যাবলেট অর্ডারিং ইন্টারফেস; প্রোটোটাইপ আইওএস টুডো অ্যাপ্লিকেশন; ডিজাইনের মূল নীতিগুলি: ভাল, খারাপ এবং কুরুচিপূর্ণ ওয়েব অনুসন্ধান; ইলাস্ট্রেটরের ভূমিকা, মূল্যের ভূমিকা: ইলাস্ট্রেটর বোঝা এবং ইলাস্ট্রেটরে ডিজাইনিং, রিফ্যাক্টর এবং অলঙ্কৃত, মূল্যের সাথে রঙের ভূমিকা, মানটিতে রঙ যুক্ত করুন; ফটোশপ পরিচিতি, ডিজাইনে ঐক্য, ফটোশপ এবং একটি ইউআই টুল, চিত্রকলার ফ্রিফর্ম; এইচটিএমএল এবং ভিজ্যুয়াল স্টুডিও কোড পরিচিতি, মার্কআপ ভাষা, ট্যাগ এবং কাঠামো শিখুন; সিএসএস পরিচিতি, ডিজাইন গাইডলাইন এবং স্টাইলিং; সিএসএস দিয়ে স্টাইলিং। স্ক্র্যাচ থেকে সিএসএস থেকে সম্পূর্ণ সামগ্রী; বিদ্যমান এইচটিএমএল উপর নতুন সিএসএস তৈরি করুন; বুটস্ট্র্যাপে বেসিক সাইট এবং অ্যাপ্লিকেশন ডেভেলপমেন্ট, একটি প্রতিক্রিয়াশীল সাইট বিকাশ করুন যা পিসি, টেবিল এবং ফোনে কাজ করবে; বুটস্ট্র্যাপে বেসিক সাইট এবং অ্যাপ ডিজাইন; আগের ওয়েবসাইটে রেস্পন্সিভ সাইটের জন্য গ্রাফিক্স ডিজাইন করুন।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("jakub-zerdzicki-wX062bi-T50-unsplash.jpg")
                                 .trimester(103)
                                 .courseCode("CSE4949")
                                 .titleEn("IT Audit: Concepts and Practice")
                                 .titleBn("আইটি অডিট: ধারণা এবং অনুশীলন")
                                 .descriptionEn("IT audit concepts and frameworks; General phases of IT audit; Internal IT audit control framework: the committee of sponsoring organizations (COSO); The impact of information technology audit process on internal controls: general controls, application controls, tests of controls; Referring case study; IT-Audit methodologies and frameworks: COBIT, ITIL, ISO 17799 etc; Practical IT-Audit methodologies development steps for enterprises completing the audit: reporting, types of auditors’ opinions, audit documentation and resources; Referring case study.")
                                 .descriptionBn("আইটি অডিট ধারণা এবং ফ্রেমওয়ার্ক; আইটি অডিটের সাধারণ পর্যায়; অভ্যন্তরীণ আইটি নিরীক্ষা নিয়ন্ত্রণ কাঠামো: স্পনসরিং সংস্থাগুলির কমিটি (সিওএসও); অভ্যন্তরীণ নিয়ন্ত্রণের উপর তথ্য প্রযুক্তি নিরীক্ষা প্রক্রিয়ার প্রভাব: সাধারণ নিয়ন্ত্রণ, অ্যাপ্লিকেশন নিয়ন্ত্রণ, নিয়ন্ত্রণের পরীক্ষা; কেস স্টাডি উল্লেখ করা; আইটি-অডিট পদ্ধতি এবং ফ্রেমওয়ার্ক: সিওবিআইটি, আইটিআইএল, আইএসও 17799 ইত্যাদি; নিরীক্ষা সম্পন্ন উদ্যোগের জন্য ব্যবহারিক আইটি-অডিট পদ্ধতি উন্নয়ন পদক্ষেপ: প্রতিবেদন, নিরীক্ষকদের মতামতের ধরণ, নিরীক্ষা ডকুমেন্টেশন এবং সম্পদ; কেস স্টাডি উল্লেখ করা।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("")
                                 .build() //
        );

        addPageHeader();
        addFormattedText("Course Offerings");

        FlowPane courseContainer = new FlowPane(30, 20);
        courseContainer.setAlignment(Pos.CENTER);
        courseContainer.setPrefWrapLength(700); // Adjust based on available space

        offeredCourses.forEach(course -> courseContainer.getChildren().add(createCourseCard(course)));

        // Wrap the FlowPane inside a ScrollPane to enable scrolling
        ScrollPane scrollPane = new ScrollPane(courseContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true); // Allow dragging with mouse
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical scrollbar as needed
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // No horizontal scrollbar

        addNode(scrollPane);
    }

    private Card createCourseCard(OfferedCourseData course) {
        var card = new Card();
        card.getStyleClass().add(Styles.ELEVATED_1);
        card.setMinWidth(350);
        card.setMaxWidth(500);

        // Load course image with cropping
        var courseImage = new ImageView(new Image(Resources.getResourceAsStream(IMAGE_PATH + course.getImgFile())));
        courseImage.setFitHeight(300);
        courseImage.setPreserveRatio(true);
        courseImage.setSmooth(true);
        courseImage.setClip(new Rectangle(500, 300)); // Crops the image to center

        // Center crop by adjusting viewport
        Image  img         = courseImage.getImage();
        double imageWidth  = img.getWidth();
        double imageHeight = img.getHeight();
        double cropWidth   = 500;  // Target width
        double cropHeight  = 300; // Target height

        if (imageWidth > cropWidth || imageHeight > cropHeight) {
            double x = (imageWidth - cropWidth) / 2;
            double y = (imageHeight - cropHeight) / 2;
            courseImage.setViewport(new Rectangle2D(x, y, cropWidth, cropHeight));
        }

        card.setSubHeader(courseImage);

        // Header with title, course code, type, and credit
        var subHeaderText = String.format("%s (%s) - %d Credits", course.getCourseCode(), course.getType(), course.getCredits());
        var header        = new Tile(course.getTitleEn(), subHeaderText);
        card.setHeader(header);

        // Body with expandable description text
        TextFlow description = createFormattedText(course.getDescriptionEn(), true);
        description.setMaxWidth(470);
        description.setPrefHeight(Region.USE_COMPUTED_SIZE);
        card.setBody(description);

        // Footer with randomized status and action button
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getChildren().addAll(new Circle(8, Color.web(randomColor())), new Label(randomStatus()));

        Button actionButton = new Button(determineActionButtonText(randomStatus()));
        actionButton.setPrefWidth(120);
        footer.getChildren().add(actionButton);
        card.setFooter(footer);

        return card;
    }

    private String randomStatus() {
        String[] statuses = {"SELECTED", "REGISTERED", "COMPLETED", "DROPPED"};
        return statuses[RANDOM.nextInt(statuses.length)];
    }

    private String determineActionButtonText(String status) {
        return switch (status) {
            case "SELECTED" -> "Unselect";
            case "REGISTERED" -> "Withdraw";
            case "COMPLETED", "DROPPED" -> "Retake";
            default -> "Select";
        };
    }

    private String randomColor() {
        String[] colors = {"#FF5733", "#33FF57", "#3357FF", "#FF33A1"};
        return colors[RANDOM.nextInt(colors.length)];
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}
