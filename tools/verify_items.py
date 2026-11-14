import json

with open('svg_files_cache.json', 'r') as f:
    svg_files = set(json.load(f))

# Define our retail icons systematically
# Each entry: (key, char, dept, extra_keywords)
# dept options:
# FRUITS, VEGETABLES, DAIRY, BAKERY, SNACKS, SWEETS, BEVERAGES,
# STAPLES, SPICES_OILS, MEAT_FISH, CLEANING, PERSONAL_CARE,
# BABY_CARE, PET_CARE, STATIONERY, KITCHENWARE, ELECTRONICS,
# HEALTH, TOYS_GIFTS, GENERAL

RAW_ITEMS = [
    # --- FRUITS ---
    ("apple", "\U0001f34e", "FRUITS", ["seb", "sev", "safarchand", "kashmiri apple", "shimla apple", "red apple", "fresh apple"]),
    ("green_apple", "\U0001f34f", "FRUITS", ["granny smith", "green apple", "sour apple", "kacha seb"]),
    ("banana", "\U0001f34c", "FRUITS", ["kela", "vazhaipazham", "yelakki", "robusta", "nendran", "cavendish", "raw banana", "pazham"]),
    ("orange", "\U0001f34a", "FRUITS", ["santra", "narangi", "kamala orange", "nagpur santra", "kinnow", "tangerine", "mandarin", "aarancu"]),
    ("lemon", "\U0001f34b", "FRUITS", ["nimbu", "elumichai", "lime", "citrus", "yellow lemon", "sour lemon", "nimbu paani"]),
    ("watermelon", "\U0001f349", "FRUITS", ["tarbooz", "tharpusani", "water melon", "tarbuj", "red melon", "summer fruit"]),
    ("grapes", "\U0001f347", "FRUITS", ["angoor", "dhrakshai", "thompson", "kishmish", "black grapes", "green grapes", "sonaka"]),
    ("strawberry", "\U0001f353", "FRUITS", ["strawberries", "berry", "fresh strawberry", "mahableshwar strawberry"]),
    ("melon", "\U0001f348", "FRUITS", ["kharbooja", "kirni pazham", "muskmelon", "cantaloupe", "honeydew", "sweet melon"]),
    ("cherries", "\U0001f352", "FRUITS", ["cherry", "red cherry", "kashmir cherry", "fresh cherry"]),
    ("peach", "\U0001f351", "FRUITS", ["aadu", "fresh peach", "stone fruit"]),
    ("mango", "\U0001f96d", "FRUITS", ["aam", "mambazham", "alphonso", "kesar", "banganapalli", "totapuri", "dasheri", "langra", "ratnagiri"]),
    ("pineapple", "\U0001f34d", "FRUITS", ["ananas", "annasi pazham", "rani pineapple", "sweet pineapple"]),
    ("coconut", "\U0001f965", "FRUITS", ["nariyal", "thengai", "fresh coconut", "tender coconut", "elaneer", "copra", "dry coconut"]),
    ("kiwi", "\U0001f95d", "FRUITS", ["kiwifruit", "green kiwi", "zespri"]),
    ("pear", "\U0001f350", "FRUITS", ["nashpati", "berikai", "green pear", "babu gosha"]),
    ("avocado", "\U0001f951", "FRUITS", ["butter fruit", "hass avocado", "guacamole"]),
    ("tomato", "\U0001f345", "FRUITS", ["tamatar", "thakkali", "desi tomato", "hybrid tomato", "red tomato"]),
    ("blueberries", "\U0001fad0", "FRUITS", ["blueberry", "fresh blueberry", "imported berries"]),
    ("olive", "\U0001fad2", "FRUITS", ["jaitun", "black olive", "green olive", "spanish olive", "figaro"]),
    ("chestnut", "\U0001f330", "FRUITS", ["singhada", "water chestnut", "dry chestnut"]),

    # --- VEGETABLES ---
    ("potato", "\U0001f954", "VEGETABLES", ["aloo", "urulaikizhangu", "batata", "pahadi aloo", "baby potato", "jyoti potato"]),
    ("carrot", "\U0001f955", "VEGETABLES", ["gajar", "karat", "red carrot", "orange carrot", "desi gajar"]),
    ("onion", "\U0001f9c5", "VEGETABLES", ["pyaz", "vengayam", "kanda", "sambhar onion", "shallots", "red onion", "spring onion", "white onion"]),
    ("garlic", "\U0001f9c4", "VEGETABLES", ["lahsun", "poondu", "lasun", "peeled garlic", "desi lahsun", "garlic pods"]),
    ("cucumber", "\U0001f952", "VEGETABLES", ["kheera", "vellarikkai", "kakdi", "salad cucumber", "english cucumber"]),
    ("broccoli", "\U0001f966", "VEGETABLES", ["hari gobhi", "green broccoli", "fresh broccoli"]),
    ("leafy_greens", "\U0001f96c", "VEGETABLES", ["palak", "spinach", "keerai", "sarson ka saag", "methi", "fenugreek", "lettuce", "salad greens"]),
    ("eggplant", "\U0001f346", "VEGETABLES", ["baingan", "kathirikai", "brinjal", "aubergine", "bharta baingan", "small brinjal"]),
    ("corn", "\U0001f33d", "VEGETABLES", ["makka", "bhutta", "cholam", "sweet corn", "corn cobs", "american corn"]),
    ("hot_pepper", "\U0001f336", "VEGETABLES", ["mirchi", "milagai", "green chilli", "red chilli", "hari mirch", "lal mirch", "guntur mirchi"]),
    ("bell_pepper", "\U0001fad1", "VEGETABLES", ["shimla mirch", "kuda milagai", "capsicum", "green capsicum", "red capsicum", "yellow capsicum"]),
    ("sweet_potato", "\U0001f360", "VEGETABLES", ["shakarkandi", "sarkaraivalli kizhangu", "yam", "sweet yam"]),
    ("mushroom", "\U0001f344", "VEGETABLES", ["kukurmutta", "kaalan", "button mushroom", "oyster mushroom", "fresh mushroom"]),
    ("peanuts", "\U0001f95c", "VEGETABLES", ["mungfali", "kadalai", "groundnut", "raw peanuts", "shengdana", "roasted peanuts"]),
    ("peas", "\U0001fad8", "VEGETABLES", ["matar", "pattani", "green peas", "fresh peas", "safal peas", "frozen peas"]),
    ("pumpkin", "\U0001f383", "VEGETABLES", ["kaddu", "poosanikai", "red pumpkin", "yellow pumpkin", "sitaphal"]),
    ("herbs", "\U0001f33f", "VEGETABLES", ["dhaniya", "kothamalli", "coriander", "mint", "pudina", "curry leaves", "kariveppilai"]),
    ("seedling", "\U0001f331", "VEGETABLES", ["sprouts", "ankurit dal", "microgreens", "moong sprouts"]),

    # --- DAIRY & EGGS ---
    ("milk_bottle", "\U0001f95b", "DAIRY", ["doodh", "dudh", "paal", "cow milk", "buffalo milk", "toned milk", "full cream milk", "amul taaza", "amul gold", "nandini", "mother dairy", "aavin"]),
    ("cheese", "\U0001f9c0", "DAIRY", ["paneer", "cheese cubes", "cheese slice", "mozzarella", "cheddar", "amul cheese", "britannia cheese", "cottage cheese"]),
    ("butter", "\U0001f9c8", "DAIRY", ["makhan", "vennai", "amul butter", "table butter", "white butter", "salted butter", "unsalted butter", "delicious butter"]),
    ("egg", "\U0001f95a", "DAIRY", ["anda", "muttai", "farm eggs", "brown eggs", "white eggs", "desi eggs", "egg tray", "poultry eggs"]),
    ("ice_cream", "\U0001f368", "DAIRY", ["ice cream tub", "vanilla ice cream", "chocolate ice cream", "amul ice cream", "kwality walls", "havmor", "vadilal", "kulfi"]),
    ("soft_ice_cream", "\U0001f366", "DAIRY", ["cone ice cream", "cornetto", "chocobar", "soft serve", "ice cream cone"]),
    ("shaved_ice", "\U0001f367", "DAIRY", ["gola", "baraf gola", "crushed ice", "ice slush", "frozen treat"]),
    ("baby_bottle", "\U0001f37c", "DAIRY", ["baby milk", "infant milk", "nan pro", "lactogen", "similac", "feeding milk"]),

    # --- BAKERY ---
    ("bread", "\U0001f35e", "BAKERY", ["white bread", "brown bread", "wheat bread", "atta bread", "milk bread", "sandwich bread", "britannia bread", "modern bread", "harvest gold"]),
    ("croissant", "\U0001f950", "BAKERY", ["butter croissant", "chocolate croissant", "french pastry", "puff"]),
    ("baguette", "\U0001f956", "BAKERY", ["french bread", "garlic bread", "bread stick", "long bread"]),
    ("flatbread", "\U0001fad3", "BAKERY", ["roti", "chapati", "naan", "kulcha", "rumali roti", "phulka", "paratha", "idiyappam"]),
    ("bagel", "\U0001f96f", "BAKERY", ["sesame bagel", "bread ring", "donut bread"]),
    ("pretzel", "\U0001f968", "BAKERY", ["salted pretzel", "twisted bread", "baked pretzel"]),
    ("pancake", "\U0001f95e", "BAKERY", ["hotcake", "crepe", "dosa", "cheela", "batter cake"]),
    ("waffle", "\U0001f9c7", "BAKERY", ["belgian waffle", "chocolate waffle", "waffle crisp"]),
    ("sandwich", "\U0001f96a", "BAKERY", ["club sandwich", "veg sandwich", "cheese sandwich", "grilled sandwich", "jumbo sandwich"]),
    ("pie", "\U0001f967", "BAKERY", ["apple pie", "veg pie", "tart", "baked pie"]),
    ("burger_bun", "\U0001f354", "BAKERY", ["bun", "burger bun", "pav", "ladi pav", "sweet bun", "cream bun"]),
    ("hotdog_bun", "\U0001f32d", "BAKERY", ["long bun", "roll", "frankie roll", "kathi roll bun"]),

    # --- SNACKS ---
    ("cookie", "\U0001f36a", "SNACKS", ["biscuit", "biscuits", "parle-g", "parle g", "good day", "britannia", "marie gold", "monaco", "krackjack", "hide and seek", "bourbon", "dark fantasy", "oreo", "tiger"]),
    ("cracker", "\U0001f9c3", "SNACKS", ["salted biscuit", "cream cracker", "snack cracker", "50-50", "time pass", "salt biscuits"]),
    ("popcorn", "\U0001f37f", "SNACKS", ["act ii", "act 2", "butter popcorn", "cheese popcorn", "caramel popcorn", "salted popcorn", "corn snack"]),
    ("potato_chips", "\U0001f35f", "SNACKS", ["chips", "wafers", "lays", "bingo", "pringles", "uncle chipps", "kurkure", "aloo wafers", "salted chips", "banana chips"]),
    ("samosa", "\U0001f95f", "SNACKS", ["dumpling", "aloo samosa", "punjabi samosa", "momo", "gujiya", "potli", "snack patty"]),
    ("taco", "\U0001f32e", "SNACKS", ["nachos", "tortilla chips", "doritos", "corn crisps", "tacos"]),
    ("burrito", "\U0001f32f", "SNACKS", ["wrap", "kathi roll", "veg roll", "spring roll", "frankie"]),
    ("stuffed_flatbread", "\U0001f959", "SNACKS", ["aloo paratha", "stuffed paratha", "kulcha", "paneer paratha", "thepla", "khakhra"]),
    ("falafel", "\U0001f9c6", "SNACKS", ["vada", "medu vada", "dal vada", "tikki", "aloo tikki", "cutlet", "pakoda", "bhajji"]),

    # --- SWEETS & CONFECTIONERY ---
    ("chocolate", "\U0001f36b", "SWEETS", ["cadbury", "dairy milk", "kitkat", "5 star", "munch", "perk", "snickers", "amul chocolate", "dark chocolate", "silk"]),
    ("candy", "\U0001f36c", "SWEETS", ["toffee", "pulse", "alpenliebe", "melody", "kopiko", "mango bite", "eclairs", "mithai"]),
    ("lollipop", "\U0001f36d", "SWEETS", ["chupa chups", "lollypop", "candy stick", "sweet pop"]),
    ("cupcake", "\U0001f9c1", "SWEETS", ["muffin", "winkies", "bar cake", "britannia cake", "cup cake"]),
    ("birthday_cake", "\U0001f382", "SWEETS", ["cake", "black forest", "chocolate cake", "pineapple cake", "pastry cake", "birthday cake"]),
    ("shortcake", "\U0001f370", "SWEETS", ["pastry", "strawberry cake", "cream slice", "sponge cake"]),
    ("pudding", "\U0001f36e", "SWEETS", ["custard", "kheer", "payasam", "caramel pudding", "falooda"]),
    ("donut", "\U0001f369", "SWEETS", ["doughnut", "chocolate donut", "glazed donut", "modak"]),
    ("honey", "\U0001f36f", "SWEETS", ["shahad", "then", "dabur honey", "patanjali honey", "pure honey", "raw honey", "wild honey"]),
    ("ladoo", "\U0001f361", "SWEETS", ["mithai", "motichoor ladoo", "besan ladoo", "gulab jamun", "rasgulla", "peda", "barfi", "haldiram sweets", "jalebi"]),
    ("moon_cake", "\U0001f96e", "SWEETS", ["sweet cake", "baklava", "traditional sweet", "sandesh"]),
    ("fortune_cookie", "\U0001f960", "SWEETS", ["crisp sweet", "cookie treat"]),

    # --- GRAINS, PULSES & STAPLES ---
    ("cooked_rice", "\U0001f35a", "STAPLES", ["chawal", "arisi", "bhat", "steamed rice", "boiled rice", "biryani rice"]),
    ("sheaf_of_rice", "\U0001f33e", "STAPLES", ["basmati", "raw rice", "sona masoori", "kolam", "ponni arisi", "daawat", "india gate", "fortune rice", "brown rice", "idli rice"]),
    ("bowl_flour", "\U0001f963", "STAPLES", ["atta", "maida", "sooji", "rava", "besan", "aashirvaad", "wheat flour", "poha", "aval", "dalia", "cornflour", "gehu ka atta"]),
    ("noodles", "\U0001f35c", "STAPLES", ["maggi", "yippee", "top ramen", "hakka noodles", "instant noodles", "chowmein", "cup noodles", "wai wai", "ramen", "sevai"]),
    ("spaghetti", "\U0001f35d", "STAPLES", ["pasta", "macaroni", "penne", "fusilli", "maggi pazzta", "durum wheat pasta"]),
    ("canned_food", "\U0001f96b", "STAPLES", ["tin can", "canned beans", "canned sweet corn", "canned pineapple", "canned mushroom"]),
    ("takeout_box", "\U0001f961", "STAPLES", ["meal box", "packed food", "ready to eat", "mtr", "kitchens of india"]),
    ("bento_box", "\U0001f371", "STAPLES", ["thali", "lunch pack", "combo meal", "tiffin"]),

    # --- SPICES, CONDIMENTS & OILS ---
    ("salt", "\U0001f9c2", "SPICES_OILS", ["namak", "uppu", "tata salt", "iodized salt", "rock salt", "sendha namak", "black salt", "kala namak"]),
    ("oil_drop", "\U0001fa78", "SPICES_OILS", ["tel", "ennai", "cooking oil", "refined oil", "mustard oil", "sarson tel", "sunflower oil", "fortune oil", "dhara", "saffola", "gemini"]),
    ("water_drop", "\U0001f4a7", "SPICES_OILS", ["groundnut oil", "kadugu ennai", "sesame oil", "gingelly oil", "til oil", "deepam oil"]),
    ("curry_rice", "\U0001f35b", "SPICES_OILS", ["masala", "curry powder", "garam masala", "everest masala", "mdh", "catch spices", "sambar powder", "rasam powder", "biryani masala", "chana masala"]),
    ("spices_herbs", "\U0001f33f", "SPICES_OILS", ["haldi", "turmeric", "manjal", "jeera", "cumin", "seeragam", "dhania", "coriander powder", "rai", "mustard seeds", "methi dana", "ajwain", "hing", "asafoetida"]),

    # --- BEVERAGES ---
    ("tea_cup", "\U00002615", "BEVERAGES", ["chai", "theeneer", "tata tea", "red label", "taj mahal", "wagh bakri", "brooke bond", "society tea", "tea powder", "chai patti", "leaf tea", "dust tea"]),
    ("green_tea", "\U0001f375", "BEVERAGES", ["tetley", "lipton green tea", "herbal tea", "chamomile", "tulsi tea", "organic india", "lemon tea"]),
    ("teapot", "\U0001fad6", "BEVERAGES", ["tea leaves", "brewed tea", "assam tea", "darjeeling tea"]),
    ("mate_drink", "\U0001f9c9", "BEVERAGES", ["herbal infusion", "ayurvedic drink", "kashayam", "kadha"]),
    ("bubble_tea", "\U0001f9cb", "BEVERAGES", ["boba", "milk tea", "shake", "thick shake"]),
    ("cup_with_straw", "\U0001f964", "BEVERAGES", ["soft drink", "coca cola", "pepsi", "thums up", "sprite", "fanta", "mirinda", "limca", "frooti", "maaza", "slice", "real juice", "tropicana", "sting", "red bull"]),
    ("beer_mug", "\U0001f37a", "BEVERAGES", ["beer", "lager", "draught beer", "malt beverage"]),
    ("beer_bottles", "\U0001f37b", "BEVERAGES", ["craft beer", "chilled beer", "bottled beer"]),
    ("wine_glass", "\U0001f377", "BEVERAGES", ["red wine", "white wine", "sula", "grape wine"]),
    ("champagne", "\U0001f37e", "BEVERAGES", ["sparkling drink", "champagne", "celebration drink", "fizzy wine"]),
    ("cocktail", "\U0001f378", "BEVERAGES", ["mocktail", "fruit punch", "mixed juice", "syrup drink"]),
    ("tropical_drink", "\U0001f379", "BEVERAGES", ["lemonade", "nimbu soda", "rooh afza", "rasna", "glucon-d", "tang", "sharbat"]),
    ("ice_cube", "\U0001f9ca", "BEVERAGES", ["ice pack", "mineral water", "drinking water", "bisleri", "kinley", "aquafina", "bailley"]),

    # --- MEAT & SEAFOOD ---
    ("poultry_leg", "\U0001f357", "MEAT_FISH", ["chicken", "kozhi", "murgh", "chicken drumstick", "chicken breast", "chicken curry cut", "boneless chicken", "broiler"]),
    ("cut_of_meat", "\U0001f969", "MEAT_FISH", ["mutton", "gosht", "aattu iraichi", "goat meat", "lamb", "buff", "pork", "beef", "keema", "minced meat"]),
    ("bacon", "\U0001f953", "MEAT_FISH", ["cured meat", "ham", "sausage", "salami"]),
    ("fish", "\U0001f41f", "MEAT_FISH", ["machli", "meen", "rohu", "katla", "hilsa", "pomfret", "surmai", "seer fish", "salmon", "fresh fish"]),
    ("fish_cake", "\U0001f365", "MEAT_FISH", ["fish fingers", "fish cutlet", "fish fillet"]),
    ("shrimp", "\U0001f990", "MEAT_FISH", ["prawns", "jheenga", "eraal", "tiger prawns", "white prawns", "cleaned prawns"]),
    ("crab", "\U0001f980", "MEAT_FISH", ["kankda", "nandu", "fresh crab", "mud crab"]),
    ("lobster", "\U0001f99e", "MEAT_FISH", ["king lobster", "crayfish", "seafood"]),
    ("squid", "\U0001f991", "MEAT_FISH", ["calamari", "kanava", "octopus", "squid rings"]),
    ("oyster", "\U0001f9aa", "MEAT_FISH", ["clams", "mussels", "shellfish"]),
    ("bone", "\U0001f9b4", "MEAT_FISH", ["soup bone", "marrow bone", "meat bones"]),
    ("chicken_whole", "\U0001f414", "MEAT_FISH", ["whole chicken", "desi murgi", "country chicken", "naatu kozhi"]),

    # --- HOUSEHOLD & CLEANING ---
    ("broom", "\U0001f9f9", "CLEANING", ["jhadu", "thodappam", "phool jhadu", "grass broom", "floor broom", "wiper", "mop"]),
    ("soap_bar", "\U0001f9fc", "CLEANING", ["vim bar", "rin soap", "surf excel bar", "dishwash bar", "detergent cake", "exo bar", "sabun"]),
    ("lotion_bottle", "\U0001f9f4", "CLEANING", ["surf excel liquid", "ariel liquid", "lizol", "harpic", "colin", "vim liquid", "comfort", "floor cleaner", "toilet cleaner", "dettol liquid"]),
    ("sponge", "\U0001f9fd", "CLEANING", ["scrub pad", "scotch brite", "dishwash sponge", "steel scrubber", "cleaning scrub", "foam pad"]),
    ("bucket", "\U0001faa3", "CLEANING", ["balti", "plastic bucket", "mug", "cleaning pail", "water bucket"]),
    ("toilet_paper", "\U0001f9fb", "CLEANING", ["tissue paper", "kitchen roll", "toilet roll", "paper napkin", "serviette", "face tissue", "origami"]),
    ("wastebasket", "\U0001f5d1", "CLEANING", ["dustbin", "garbage bags", "trash bags", "waste bin"]),
    ("plunger", "\U0001fa9f", "CLEANING", ["drain cleaner", "sink cleaner", "toilet plunger"]),

    # --- PERSONAL CARE & HYGIENE ---
    ("toothbrush", "\U0001faa5", "PERSONAL_CARE", ["colgate brush", "oral b", "toothpaste", "colgate", "pepsodent", "close up", "sensodyne", "dant kanti", "dabur red"]),
    ("razor", "\U0001fa92", "PERSONAL_CARE", ["gillette", "shaving blade", "shaving razor", "shaving foam", "shaving cream", "aftershave", "ustara"]),
    ("comb", "\U0001f487", "PERSONAL_CARE", ["kanghi", "seepu", "hair comb", "hair brush", "hair care"]),
    ("lipstick", "\U0001f484", "PERSONAL_CARE", ["lip balm", "vaseline", "lakme", "maybelline", "cosmetics", "lip gloss", "chapstick"]),
    ("nail_polish", "\U0001f485", "PERSONAL_CARE", ["nail paint", "nail enamel", "nail polish remover", "manicure"]),
    ("mirror", "\U0001fa9e", "PERSONAL_CARE", ["sheesha", "pocket mirror", "shaving mirror"]),
    ("scissors", "\U00002702", "PERSONAL_CARE", ["nail cutter", "hair scissors", "grooming scissor"]),
    ("safety_pin", "\U0001f9f7", "PERSONAL_CARE", ["sanitary pads", "whisper", "stayfree", "sofy", "cotton buds", "ear buds", "cotton roll", "bandage tape"]),
    ("shampoo", "\U0001f9f4", "PERSONAL_CARE", ["clinic plus", "head and shoulders", "sunsilk", "dove shampoo", "pantene", "tresemme", "hair oil", "parachute", "vatika", "bajaj almond"]),
    ("bath_soap", "\U0001f9fc", "PERSONAL_CARE", ["dettol soap", "lifebuoy", "lux", "pears", "cinthol", "santoor", "fiama", "body wash", "handwash", "dettol handwash"]),

    # --- BABY CARE ---
    ("baby_face", "\U0001f476", "BABY_CARE", ["pampers", "huggies", "mamy poko pants", "baby diapers", "baby wipes", "johnsons baby", "himalaya baby"]),
    ("teddy_bear", "\U0001f9f8", "BABY_CARE", ["soft toy", "baby toy", "plush toy", "rattle"]),
    ("baby_crib", "\U0001f6cc", "BABY_CARE", ["jhoola", "baby cot", "baby bed", "pram"]),

    # --- PET CARE ---
    ("dog", "\U0001f415", "PET_CARE", ["dog food", "pedigree", "drools", "royal canin", "puppy food", "dog biscuits", "dog treats", "meat up"]),
    ("cat", "\U0001f408", "PET_CARE", ["cat food", "whiskas", "me-o", "purepet", "kitten food", "cat treats"]),
    ("paw_prints", "\U0001f43e", "PET_CARE", ["pet grooming", "pet shampoo", "pet accessories", "pet care"]),
    ("bird", "\U0001f426", "PET_CARE", ["bird food", "seeds", "parrot food", "lovebirds food"]),
    ("rabbit", "\U0001f407", "PET_CARE", ["rabbit food", "hay", "bunny pellets"]),
    ("hamster", "\U0001f439", "PET_CARE", ["hamster food", "guinea pig food"]),

    # --- STATIONERY & OFFICE ---
    ("ballpoint_pen", "\U0001f58a", "STATIONERY", ["pen", "reynolds", "cello", "flair", "ball pen", "gel pen", "trimax", "uniball", "hauser", "ink pen"]),
    ("pencil", "\U0000270f", "STATIONERY", ["apsara pencil", "natraj", "doms", "lead pencil", "drawing pencil", "eraser", "sharpener"]),
    ("fountain_pen", "\U0001f58b", "STATIONERY", ["ink pen", "camlin", "parker", "fountain pen", "ink bottle"]),
    ("marker", "\U0001f58d", "STATIONERY", ["highlighter", "whiteboard marker", "permanent marker", "sketch pen", "camlin marker", "crayons", "color pencils"]),
    ("spiral_notebook", "\U0001f4d3", "STATIONERY", ["classmate notebook", "copy", "rough notebook", "long notebook", "spiral book", "navneet"]),
    ("ruled_notebook", "\U0001f4d4", "STATIONERY", ["single line notebook", "four line notebook", "square line", "school notebook"]),
    ("ledger", "\U0001f4d5", "STATIONERY", ["khata book", "bahi khata", "register", "attendance register", "cash book", "bill book", "voucher pad"]),
    ("books", "\U0001f4da", "STATIONERY", ["textbook", "guide book", "drawing book", "chart paper"]),
    ("paperclip", "\U0001f4ce", "STATIONERY", ["gems clip", "binder clip", "u clip", "paper clips", "stapler pins"]),
    ("straight_ruler", "\U0001f4cf", "STATIONERY", ["scale", "plastic scale", "steel ruler", "30cm scale", "15cm scale"]),
    ("triangle_ruler", "\U0001f4d0", "STATIONERY", ["geometry box", "set square", "compass", "protractor", "camlin flora"]),
    ("envelope", "\U00002709", "STATIONERY", ["post envelope", "brown envelope", "office envelope", "cover", "shagun lifafa"]),
    ("memo", "\U0001f4dd", "STATIONERY", ["post it", "sticky notes", "notepad", "scratch pad", "scribble pad"]),
    ("pushpin", "\U0001f4cc", "STATIONERY", ["thumb pin", "board pin", "soft board pin", "tack"]),
    ("calendar", "\U0001f4c5", "STATIONERY", ["panchang", "desk calendar", "wall calendar", "planner"]),
    ("clipboard", "\U0001f4cb", "STATIONERY", ["exam pad", "writing pad", "clip board"]),
    ("file_folder", "\U0001f4c1", "STATIONERY", ["cobra file", "display folder", "ring binder", "plastic file", "document folder"]),
    ("backpack", "\U0001f392", "STATIONERY", ["school bag", "tuition bag", "pencil pouch", "pencil box"]),
    ("glue_tape", "\U0001f516", "STATIONERY", ["fevistick", "fevicol", "glue", "cello tape", "brown tape", "masking tape", "double side tape"]),

    # --- KITCHENWARE & TABLEWARE ---
    ("kitchen_knife", "\U0001f52a", "KITCHENWARE", ["chaku", "knife", "vegetable knife", "chopping knife", "kitchen cutter"]),
    ("fork_and_knife", "\U0001f374", "KITCHENWARE", ["cutlery", "fork", "table knife", "spoon fork set"]),
    ("spoon", "\U0001f944", "KITCHENWARE", ["chamach", "steel spoon", "tablespoon", "teaspoon", "soup spoon", "serving spoon"]),
    ("plate", "\U0001f37d", "KITCHENWARE", ["thali", "dinner plate", "steel plate", "melamine plate", "quarter plate"]),
    ("cooking_pot", "\U0001f372", "KITCHENWARE", ["patila", "bhagona", "degchi", "cooker", "pressure cooker", "saucepan", "milk pan", "handi"]),
    ("frying_pan", "\U0001f373", "KITCHENWARE", ["kadhai", "tawa", "dosa tawa", "roti tawa", "fry pan", "non stick pan", "prestige", "hawkins"]),
    ("chopsticks", "\U0001f962", "KITCHENWARE", ["wooden chopsticks", "noodle sticks"]),
    ("thermos", "\U0001f376", "KITCHENWARE", ["milton flask", "thermos flask", "hot water bottle", "insulated flask", "water bottle", "school bottle"]),

    # --- ELECTRONICS & ELECTRICALS ---
    ("battery", "\U0001f50b", "ELECTRONICS", ["duracell", "everyeady", "aa cell", "aaa battery", "remote cell", "nippo", "panasonic"]),
    ("electric_plug", "\U0001f50c", "ELECTRONICS", ["plug", "socket", "extension board", "adapter", "usb cable", "charging cable", "mobile charger"]),
    ("light_bulb", "\U0001f4a1", "ELECTRONICS", ["led bulb", "philips bulb", "wipro bulb", "cfl", "tube light", "9w bulb", "night bulb"]),
    ("flashlight", "\U0001f526", "ELECTRONICS", ["torch", "led torch", "emergency light", "rechargeable torch"]),
    ("phone", "\U0001f4f1", "ELECTRONICS", ["mobile phone", "screen guard", "phone cover", "sim card", "tempered glass"]),
    ("headphones", "\U0001f3a7", "ELECTRONICS", ["earphones", "earbuds", "boat", "noise", "bluetooth earphones", "wired headset"]),
    ("laptop", "\U0001f4bb", "ELECTRONICS", ["computer", "mouse", "keyboard", "pendrive", "usb drive", "san disk"]),
    ("tablet", "\U0001f4f2", "ELECTRONICS", ["ipad", "tab", "display tablet"]),
    ("alarm_clock", "\U000023f0", "ELECTRONICS", ["ghadi", "wall clock", "table clock", "ajanta clock"]),
    ("wrist_watch", "\U0000231a", "ELECTRONICS", ["watch", "titan", "fastrack", "smart watch", "wrist watch"]),
    ("lock", "\U0001f512", "ELECTRONICS", ["tala", "padlock", "godrej lock", "brass lock", "door lock"]),
    ("key", "\U0001f511", "ELECTRONICS", ["chabi", "keys", "spare key"]),
    ("hammer", "\U0001f528", "ELECTRONICS", ["hathoda", "nail hammer", "hardware tool"]),
    ("screwdriver", "\U0001fa9b", "ELECTRONICS", ["tester", "screw driver", "hardware kit"]),

    # --- MEDICINE & HEALTH ---
    ("pill", "\U0001f48a", "HEALTH", ["tablet", "capsule", "paracetamol", "crocin", "dolo 650", "pain killer", "saridon", "disprin", "combiflam", "gelusil", "pudina hara", "eno", "vitamin c", "becosules"]),
    ("bandage", "\U0001fa79", "HEALTH", ["band aid", "plaster", "hansaplast", "crepe bandage", "first aid tape"]),
    ("syringe", "\U0001f489", "HEALTH", ["dispo van", "injection", "insulin syringe"]),
    ("stethoscope", "\U0001fa7a", "HEALTH", ["bp monitor", "blood pressure check", "health tracker"]),
    ("thermometer", "\U0001f321", "HEALTH", ["digital thermometer", "fever check", "temperature"]),
    ("face_mask", "\U0001f637", "HEALTH", ["surgical mask", "n95 mask", "cotton mask", "protective mask"]),
    ("first_aid", "\U000026d1", "HEALTH", ["first aid kit", "burnol", "savlon", "betadine", "moov", "iodex", "volini", "fast relief", "vicks vaporub", "amrutanjan"]),

    # --- TOYS, GIFTS & PUJA ---
    ("gift_box", "\U0001f381", "TOYS_GIFTS", ["gift pack", "present", "gift wrapper", "cadbury celebrations", "gift hamper"]),
    ("balloon", "\U0001f388", "TOYS_GIFTS", ["birthday balloon", "metallic balloon", "party decoration", "air balloon"]),
    ("party_popper", "\U0001f389", "TOYS_GIFTS", ["party blast", "birthday popper", "celebration spray", "snow spray"]),
    ("ribbon", "\U0001f380", "TOYS_GIFTS", ["gift ribbon", "satin ribbon", "decoration ribbon"]),
    ("confetti", "\U0001f38a", "TOYS_GIFTS", ["party confetti", "sparkle balls", "decoration"]),
    ("candle", "\U0001f56f", "TOYS_GIFTS", ["birthday candle", "wax candle", "tealight candle", "fragrance candle", "white candle"]),
    ("diya_lamp", "\U0001fa94", "TOYS_GIFTS", ["deepak", "mitti diya", "puja diya", "brass diya", "diwali diya", "agarbatti", "dhoop", "kapoor", "camphor", "cotton wicks", "cycle agarbatti", "moksh"]),
    ("bell", "\U0001f514", "TOYS_GIFTS", ["puja ghanti", "brass bell", "temple bell"]),
    ("playing_cards", "\U0001f0cf", "TOYS_GIFTS", ["taash", "cards", "rummy cards", "uno cards"]),
    ("dice", "\U0001f3b2", "TOYS_GIFTS", ["ludo", "snakes and ladders", "board game", "game die"]),
    ("chess_pawn", "\U0000265f", "TOYS_GIFTS", ["shatranj", "chess board", "strategy game"]),
    ("puzzle", "\U0001f9e9", "TOYS_GIFTS", ["jigsaw puzzle", "rubiks cube", "educational toy"]),
    ("video_game", "\U0001f3ae", "TOYS_GIFTS", ["game controller", "joystick", "toy console"]),
    ("yo_yo", "\U0001fa80", "TOYS_GIFTS", ["spinning toy", "yoyo"]),
    ("kite", "\U0001fa81", "TOYS_GIFTS", ["patang", "manjha", "sankranti kite"]),
    ("nesting_dolls", "\U0001fa86", "TOYS_GIFTS", ["doll", "wooden doll", "barbie doll", "action figure"]),
    ("firecracker", "\U0001f9e8", "TOYS_GIFTS", ["patakhe", "diwali crackers", "sparklers", "fuljhadi", "anaar", "chakri"]),
    ("greeting_card", "\U0001f48c", "TOYS_GIFTS", ["birthday card", "greeting card", "invitation card"]),

    # --- GENERAL RETAIL & PACKAGING ---
    ("shopping_cart", "\U0001f6d2", "GENERAL", ["supermarket cart", "trolley", "grocery cart", "dukaan cart"]),
    ("shopping_bags", "\U0001f6cd", "GENERAL", ["carry bag", "cloth bag", "paper bag", "jute bag", "shopping bag"]),
    ("package", "\U0001f4e6", "GENERAL", ["parcel", "carton", "box", "delivery box", "courier pack"]),
    ("storefront", "\U0001f3ea", "GENERAL", ["dukaan", "shop", "kirana", "supermarket", "store", "retail"]),
    ("price_tag", "\U0001f3f7", "GENERAL", ["label", "barcode tag", "offer tag", "discount label", "price sticker"]),
    ("receipt", "\U0001f9fe", "GENERAL", ["bill", "cash receipt", "invoice", "payment slip"]),
    ("coin", "\U0001fa99", "GENERAL", ["rupee", "change", "sikka", "currency"]),
    ("banknote", "\U0001f4b5", "GENERAL", ["cash", "rupee note", "currency note", "money"]),
    ("shopping_basket", "\U0001f9fa", "GENERAL", ["tokri", "plastic basket", "hand basket"]),
    ("delivery_truck", "\U0001f69a", "GENERAL", ["home delivery", "tempo", "transport", "logistics"])
]

print(f"Total raw curated items defined: {len(RAW_ITEMS)}")
missing = []
for k, char, dept, kw in RAW_ITEMS:
    cp = '_'.join(f'{ord(c):x}' for c in char)
    fname = f'emoji_u{cp}.svg'
    if fname not in svg_files:
        missing.append((k, cp, fname))

if missing:
    print(f"ERROR: {len(missing)} items missing from Noto SVG files:")
    for m in missing:
        print("  ", m)
else:
    print("SUCCESS: All curated items exist in the official Noto Emoji repository!")
