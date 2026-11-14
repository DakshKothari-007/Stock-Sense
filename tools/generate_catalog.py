import os
import sys
import json
import urllib.request
import subprocess
from collections import defaultdict

def main():
    with open('svg_files_cache.json', 'r') as f:
        svg_cache = set(json.load(f))

    with open('node_modules/cldr-annotations-modern/annotations/en/annotations.json', 'r', encoding='utf-8') as f:
        en_cldr = json.load(f)['annotations']['annotations']

    with open('node_modules/cldr-annotations-modern/annotations/hi/annotations.json', 'r', encoding='utf-8') as f:
        hi_cldr = json.load(f)['annotations']['annotations']

    with open('node_modules/cldr-annotations-modern/annotations/ta/annotations.json', 'r', encoding='utf-8') as f:
        ta_cldr = json.load(f)['annotations']['annotations']

    ITEMS = [
        # ==========================================
        # 1. FRUITS & BERRIES (30 items)
        # ==========================================
        ("apple", "\U0001f34e", "FRUITS", ["seb", "sev", "सेब", "ஆப்பிள்", "safarchand", "kashmiri apple", "shimla apple", "red apple", "fresh apple"]),
        ("green_apple", "\U0001f34f", "FRUITS", ["granny smith", "green apple", "sour apple", "kacha seb"]),
        ("banana", "\U0001f34c", "FRUITS", ["kela", "केला", "வாழைப்பழம்", "vazhaipazham", "yelakki", "robusta", "nendran", "cavendish", "raw banana", "pazham"]),
        ("orange", "\U0001f34a", "FRUITS", ["santra", "narangi", "संतरा", "ஆரஞ்சு", "kamala orange", "nagpur santra", "kinnow", "tangerine", "mandarin", "aarancu"]),
        ("lemon", "\U0001f34b", "FRUITS", ["nimbu", "नींबू", "எலுமிச்சை", "elumichai", "lime", "citrus", "yellow lemon", "sour lemon", "nimbu paani"]),
        ("watermelon", "\U0001f349", "FRUITS", ["tarbooz", "तरबूज", "தர்பூசணி", "tharpusani", "water melon", "tarbuj", "red melon", "summer fruit"]),
        ("grapes", "\U0001f347", "FRUITS", ["angoor", "अंगूर", "திராட்சை", "dhrakshai", "thompson", "kishmish", "black grapes", "green grapes", "sonaka"]),
        ("strawberry", "\U0001f353", "FRUITS", ["strawberries", "berry", "fresh strawberry", "mahableshwar strawberry"]),
        ("melon", "\U0001f348", "FRUITS", ["kharbooja", "खरबूजा", "kirni pazham", "muskmelon", "cantaloupe", "honeydew", "sweet melon"]),
        ("cherries", "\U0001f352", "FRUITS", ["cherry", "red cherry", "kashmir cherry", "fresh cherry"]),
        ("peach", "\U0001f351", "FRUITS", ["aadu", "fresh peach", "stone fruit"]),
        ("mango", "\U0001f96d", "FRUITS", ["aam", "आम", "மாம்பழம்", "mambazham", "alphonso", "kesar", "banganapalli", "totapuri", "dasheri", "langra", "ratnagiri"]),
        ("pineapple", "\U0001f34d", "FRUITS", ["ananas", "अनानास", "அன்னாசி", "annasi pazham", "rani pineapple", "sweet pineapple"]),
        ("coconut", "\U0001f965", "FRUITS", ["nariyal", "नारियल", "தேங்காய்", "thengai", "fresh coconut", "tender coconut", "elaneer", "copra", "dry coconut"]),
        ("kiwi", "\U0001f95d", "FRUITS", ["kiwifruit", "green kiwi", "zespri"]),
        ("pear", "\U0001f350", "FRUITS", ["nashpati", "नाशपाती", "berikai", "green pear", "babu gosha"]),
        ("avocado", "\U0001f951", "FRUITS", ["butter fruit", "hass avocado", "guacamole"]),
        ("tomato", "\U0001f345", "FRUITS", ["tamatar", "टमाटर", "தக்காளி", "thakkali", "desi tomato", "hybrid tomato", "red tomato"]),
        ("blueberries", "\U0001fad0", "FRUITS", ["blueberry", "fresh blueberry", "imported berries"]),
        ("olive", "\U0001fad2", "FRUITS", ["jaitun", "black olive", "green olive", "spanish olive", "figaro"]),
        ("chestnut", "\U0001f330", "FRUITS", ["singhada", "water chestnut", "dry chestnut"]),

        # ==========================================
        # 2. VEGETABLES & GREENS (25 items)
        # ==========================================
        ("potato", "\U0001f954", "VEGETABLES", ["aloo", "आलू", "உருளைக்கிழங்கு", "urulaikizhangu", "batata", "pahadi aloo", "baby potato", "jyoti potato", "potatoe"]),
        ("carrot", "\U0001f955", "VEGETABLES", ["gajar", "गाजर", "கேரட்", "karat", "red carrot", "orange carrot", "desi gajar"]),
        ("onion", "\U0001f9c5", "VEGETABLES", ["pyaz", "प्याज़", "प्याज", "வெங்காயம்", "vengayam", "kanda", "sambhar onion", "shallots", "red onion", "spring onion", "white onion"]),
        ("garlic", "\U0001f9c4", "VEGETABLES", ["lahsun", "लहसुन", "பூண்டு", "poondu", "lasun", "peeled garlic", "desi lahsun", "garlic pods"]),
        ("cucumber", "\U0001f952", "VEGETABLES", ["kheera", "खीरा", "வெள்ளரிக்காய்", "vellarikkai", "kakdi", "salad cucumber", "english cucumber"]),
        ("broccoli", "\U0001f966", "VEGETABLES", ["hari gobhi", "green broccoli", "fresh broccoli"]),
        ("leafy_greens", "\U0001f96c", "VEGETABLES", ["palak", "पालक", "கீரை", "keerai", "spinach", "sarson ka saag", "methi", "fenugreek", "lettuce", "salad greens"]),
        ("eggplant", "\U0001f346", "VEGETABLES", ["baingan", "बैंगन", "கத்தரிக்காய்", "kathirikai", "brinjal", "aubergine", "bharta baingan", "small brinjal"]),
        ("corn", "\U0001f33d", "VEGETABLES", ["makka", "मक्का", "மக்காச்சோளம்", "bhutta", "cholam", "sweet corn", "corn cobs", "american corn"]),
        ("hot_pepper", "\U0001f336", "VEGETABLES", ["mirchi", "मिर्ची", "மிளகாய்", "milagai", "green chilli", "red chilli", "hari mirch", "lal mirch", "guntur mirchi"]),
        ("bell_pepper", "\U0001fad1", "VEGETABLES", ["shimla mirch", "शिमला मिर्च", "குடை மிளகாய்", "kuda milagai", "capsicum", "green capsicum", "red capsicum", "yellow capsicum"]),
        ("sweet_potato", "\U0001f360", "VEGETABLES", ["shakarkandi", "शकरकंद", "sarkaraivalli kizhangu", "yam", "sweet yam"]),
        ("mushroom", "\U0001f344", "VEGETABLES", ["kukurmutta", "मशरूम", "காளான்", "kaalan", "button mushroom", "oyster mushroom", "fresh mushroom"]),
        ("peanuts", "\U0001f95c", "VEGETABLES", ["mungfali", "मूंगफली", "வேர்க்கடலை", "kadalai", "groundnut", "raw peanuts", "shengdana", "roasted peanuts"]),
        ("pumpkin", "\U0001f383", "VEGETABLES", ["kaddu", "कद्दू", "பூசணிக்காய்", "poosanikai", "red pumpkin", "yellow pumpkin", "sitaphal"]),
        ("herbs", "\U0001f33f", "VEGETABLES", ["dhaniya", "धनिया", "கொத்தமல்லி", "kothamalli", "coriander", "mint", "पुदीना", "pudina", "curry leaves", "kariveppilai"]),
        ("seedling", "\U0001f331", "VEGETABLES", ["sprouts", "ankurit dal", "microgreens", "moong sprouts", "peas"]),
        ("potted_plant", "\U0001fab4", "VEGETABLES", ["kitchen garden", "pudina plant", "tulsi plant"]),

        # ==========================================
        # 3. DAIRY & EGGS (15 items)
        # ==========================================
        ("milk_bottle", "\U0001f95b", "DAIRY", ["doodh", "dudh", "दूध", "பால்", "paal", "pasum paal", "cow milk", "buffalo milk", "toned milk", "full cream milk", "amul taaza", "amul gold", "nandini", "mother dairy", "aavin"]),
        ("cheese", "\U0001f9c0", "DAIRY", ["paneer", "पनीर", "பன்னீர்", "cheese cubes", "cheese slice", "mozzarella", "cheddar", "amul cheese", "britannia cheese", "cottage cheese"]),
        ("butter", "\U0001f9c8", "DAIRY", ["makhan", "मक्खन", "வெண்ணெய்", "vennai", "ghee", "घी", "நெய்", "ney", "amul butter", "table butter", "white butter", "salted butter", "unsalted butter", "delicious butter"]),
        ("egg", "\U0001f95a", "DAIRY", ["anda", "अंडा", "முட்டை", "muttai", "farm eggs", "brown eggs", "white eggs", "desi eggs", "egg tray", "poultry eggs"]),
        ("ice_cream", "\U0001f368", "DAIRY", ["ice cream tub", "vanilla ice cream", "chocolate ice cream", "amul ice cream", "kwality walls", "havmor", "vadilal", "kulfi"]),
        ("soft_ice_cream", "\U0001f366", "DAIRY", ["cone ice cream", "cornetto", "chocobar", "soft serve", "ice cream cone"]),
        ("shaved_ice", "\U0001f367", "DAIRY", ["gola", "baraf gola", "crushed ice", "ice slush", "frozen treat"]),
        ("baby_bottle", "\U0001f37c", "DAIRY", ["baby milk", "infant milk", "nan pro", "lactogen", "similac", "feeding milk"]),
        ("fondue", "\U0001fad5", "DAIRY", ["cheese fondue", "curd", "dahi", "दही", "தயிர்", "thayir", "amul masti dahi", "yogurt", "greek yogurt"]),

        # ==========================================
        # 4. BAKERY & BREADS (15 items)
        # ==========================================
        ("bread", "\U0001f35e", "BAKERY", ["bread", "ब्रेड", "ரொட்டி", "white bread", "brown bread", "wheat bread", "atta bread", "milk bread", "sandwich bread", "britannia bread", "modern bread", "harvest gold"]),
        ("croissant", "\U0001f950", "BAKERY", ["butter croissant", "chocolate croissant", "french pastry", "puff"]),
        ("baguette", "\U0001f956", "BAKERY", ["french bread", "garlic bread", "bread stick", "long bread"]),
        ("flatbread", "\U0001fad3", "BAKERY", ["roti", "रोटी", "chapati", "चपाती", "naan", "नान", "kulcha", "rumali roti", "phulka", "paratha", "idiyappam"]),
        ("bagel", "\U0001f96f", "BAKERY", ["sesame bagel", "bread ring", "donut bread"]),
        ("pretzel", "\U0001f968", "BAKERY", ["salted pretzel", "twisted bread", "baked pretzel"]),
        ("pancake", "\U0001f95e", "BAKERY", ["hotcake", "crepe", "dosa", "தோசை", "cheela", "batter cake"]),
        ("waffle", "\U0001f9c7", "BAKERY", ["belgian waffle", "chocolate waffle", "waffle crisp"]),
        ("sandwich", "\U0001f96a", "BAKERY", ["club sandwich", "veg sandwich", "cheese sandwich", "grilled sandwich", "jumbo sandwich"]),
        ("pie", "\U0001f967", "BAKERY", ["apple pie", "veg pie", "tart", "baked pie"]),
        ("burger_bun", "\U0001f354", "BAKERY", ["bun", "बर्गर", "burger bun", "pav", "ladi pav", "sweet bun", "cream bun"]),
        ("hotdog_bun", "\U0001f32d", "BAKERY", ["long bun", "roll", "frankie roll", "kathi roll bun"]),

        # ==========================================
        # 5. SNACKS & NAMKEEN (15 items)
        # ==========================================
        ("cookie", "\U0001f36a", "SNACKS", ["biscuit", "biscuits", "बिस्कुट", "பிஸ்கட்", "parle-g", "parle g", "good day", "britannia", "marie gold", "monaco", "krackjack", "hide and seek", "bourbon", "dark fantasy", "oreo", "tiger"]),
        ("cracker", "\U0001f9c3", "SNACKS", ["salted biscuit", "cream cracker", "snack cracker", "50-50", "time pass", "salt biscuits"]),
        ("popcorn", "\U0001f37f", "SNACKS", ["act ii", "act 2", "butter popcorn", "cheese popcorn", "caramel popcorn", "salted popcorn", "corn snack"]),
        ("potato_chips", "\U0001f35f", "SNACKS", ["chips", "wafers", "lays", "bingo", "pringles", "uncle chipps", "kurkure", "aloo wafers", "salted chips", "banana chips"]),
        ("samosa", "\U0001f95f", "SNACKS", ["dumpling", "समौसा", "samosa", "aloo samosa", "punjabi samosa", "momo", "gujiya", "potli", "snack patty"]),
        ("taco", "\U0001f32e", "SNACKS", ["nachos", "tortilla chips", "doritos", "corn crisps", "tacos"]),
        ("burrito", "\U0001f32f", "SNACKS", ["wrap", "kathi roll", "veg roll", "spring roll", "frankie"]),
        ("stuffed_flatbread", "\U0001f959", "SNACKS", ["aloo paratha", "stuffed paratha", "kulcha", "paneer paratha", "thepla", "khakhra"]),
        ("falafel", "\U0001f9c6", "SNACKS", ["vada", "வடை", "medu vada", "dal vada", "tikki", "aloo tikki", "cutlet", "pakoda", "bhajji"]),
        ("sev_bhujia", "\U0001f963", "SNACKS", ["bhujia", "sev", "भुजिया", "सेव", "namkeen", "नमकीन", "haldiram bhujia", "bikanervala", "ratlami sev", "mixture namkeen", "chivda"]),

        # ==========================================
        # 6. SWEETS & DESSERTS (15 items)
        # ==========================================
        ("chocolate", "\U0001f36b", "SWEETS", ["chocolate", "चॉकलेट", "choclate", "cadbury", "dairy milk", "kitkat", "5 star", "munch", "perk", "snickers", "amul chocolate", "dark chocolate", "silk"]),
        ("candy", "\U0001f36c", "SWEETS", ["candy", "toffee", "टाॅफी", "pulse", "alpenliebe", "melody", "kopiko", "mango bite", "eclairs", "mithai"]),
        ("lollipop", "\U0001f36d", "SWEETS", ["chupa chups", "lollypop", "candy stick", "sweet pop"]),
        ("cupcake", "\U0001f9c1", "SWEETS", ["muffin", "winkies", "bar cake", "britannia cake", "cup cake"]),
        ("birthday_cake", "\U0001f382", "SWEETS", ["cake", "केक", "black forest", "chocolate cake", "pineapple cake", "pastry cake", "birthday cake"]),
        ("shortcake", "\U0001f370", "SWEETS", ["pastry", "पेस्ट्री", "strawberry cake", "cream slice", "sponge cake"]),
        ("pudding", "\U0001f36e", "SWEETS", ["custard", "kheer", "खीर", "payasam", "பாயாசம்", "caramel pudding", "falooda"]),
        ("donut", "\U0001f369", "SWEETS", ["doughnut", "chocolate donut", "glazed donut", "modak", "मोदक"]),
        ("honey", "\U0001f36f", "SWEETS", ["shahad", "शहद", "தேன்", "then", "dabur honey", "patanjali honey", "pure honey", "raw honey", "wild honey", "gur", "गुड़", "jaggery", "வெல்லம்", "vellam"]),
        ("ladoo", "\U0001f361", "SWEETS", ["mithai", "मिठाई", "motichoor ladoo", "लड्डू", "besan ladoo", "gulab jamun", "गुलाब जामुन", "rasgulla", "रसगुल्ला", "peda", "barfi", "haldiram sweets", "jalebi", "ஜிலேபி"]),
        ("moon_cake", "\U0001f96e", "SWEETS", ["sweet cake", "baklava", "traditional sweet", "sandesh"]),
        ("fortune_cookie", "\U0001f960", "SWEETS", ["crisp sweet", "cookie treat"]),

        # ==========================================
        # 7. GRAINS, STAPLES & PULSES (15 items)
        # ==========================================
        ("cooked_rice", "\U0001f35a", "STAPLES", ["chawal", "चावल", "அரிசி", "arisi", "bhat", "steamed rice", "boiled rice", "biryani rice", "rice"]),
        ("sheaf_of_rice", "\U0001f33e", "STAPLES", ["basmati", "raw rice", "sona masoori", "kolam", "ponni arisi", "daawat", "india gate", "fortune rice", "brown rice", "idli rice", "dal", "दाल", "பருப்பு", "paruppu", "toor dal", "moong dal", "urad dal", "chana dal", "rajma"]),
        ("bowl_flour", "\U0001f963", "STAPLES", ["atta", "आटा", "maida", "मैदा", "sooji", "सूजी", "rava", "ரவை", "besan", "बेसन", "aashirvaad", "wheat flour", "गोहूं का आटा", "poha", "पोहा", "aval", "dalia", "cornflour", "gehu ka atta"]),
        ("noodles", "\U0001f35c", "STAPLES", ["maggi", "मैगी", "yippee", "top ramen", "hakka noodles", "instant noodles", "chowmein", "cup noodles", "wai wai", "ramen", "sevai", "semiya"]),
        ("spaghetti", "\U0001f35d", "STAPLES", ["pasta", "பாஸ்தா", "macaroni", "penne", "fusilli", "maggi pazzta", "durum wheat pasta"]),
        ("canned_food", "\U0001f96b", "STAPLES", ["tin can", "canned beans", "canned sweet corn", "canned pineapple", "canned mushroom"]),
        ("takeout_box", "\U0001f961", "STAPLES", ["meal box", "packed food", "ready to eat", "mtr", "kitchens of india"]),
        ("bento_box", "\U0001f371", "STAPLES", ["thali", "lunch pack", "combo meal", "tiffin"]),

        # ==========================================
        # 8. SPICES, CONDIMENTS & OILS (15 items)
        # ==========================================
        ("salt", "\U0001f9c2", "SPICES_OILS", ["namak", "नमक", "உப்பு", "uppu", "tata salt", "iodized salt", "rock salt", "sendha namak", "black salt", "kala namak", "sugar", "चीनी", "cheeni", "சர்க்கரை", "sakkarai"]),
        ("oil_drop", "\U0001fa78", "SPICES_OILS", ["oil", "cooking oil", "tel", "तेल", "எண்ணெய்", "ennai", "refined oil", "mustard oil", "sarson tel", "सरसों का तेल", "sunflower oil", "fortune oil", "dhara", "saffola", "gemini", "coconut oil", "nariyal tel", "தேங்காய் எண்ணெய்", "parachute oil"]),
        ("water_drop", "\U0001f4a7", "SPICES_OILS", ["groundnut oil", "kadugu ennai", "sesame oil", "gingelly oil", "til oil", "deepam oil"]),
        ("curry_rice", "\U0001f35b", "SPICES_OILS", ["masala", "मसाला", "மசாலா", "curry powder", "garam masala", "गरम मसाला", "everest masala", "mdh", "catch spices", "sambar powder", "rasam powder", "biryani masala", "chana masala"]),

        # ==========================================
        # 9. BEVERAGES & DRINKS (20 items)
        # ==========================================
        ("tea_cup", "\U00002615", "BEVERAGES", ["tea", "chai", "चाय", "டீ", "theeneer", "tata tea", "red label", "taj mahal", "wagh bakri", "brooke bond", "society tea", "tea powder", "chai patti", "चाय पत्ती", "டீ தூள்", "leaf tea", "dust tea", "coffee", "कॉफ़ी", "காபி", "kaapi", "bru", "nescafe", "filter coffee"]),
        ("green_tea", "\U0001f375", "BEVERAGES", ["tetley", "lipton green tea", "herbal tea", "chamomile", "tulsi tea", "organic india", "lemon tea"]),
        ("teapot", "\U0001fad6", "BEVERAGES", ["tea leaves", "brewed tea", "assam tea", "darjeeling tea", "kettle"]),
        ("mate_drink", "\U0001f9c9", "BEVERAGES", ["herbal infusion", "ayurvedic drink", "kashayam", "kadha"]),
        ("bubble_tea", "\U0001f9cb", "BEVERAGES", ["boba", "milk tea", "shake", "thick shake", "horlicks", "bournvita", "boost", "complan"]),
        ("cup_with_straw", "\U0001f964", "BEVERAGES", ["soft drink", "coca cola", "pepsi", "thums up", "sprite", "fanta", "mirinda", "limca", "frooti", "maaza", "slice", "real juice", "tropicana", "sting", "red bull"]),
        ("beer_mug", "\U0001f37a", "BEVERAGES", ["beer", "lager", "draught beer", "malt beverage"]),
        ("beer_bottles", "\U0001f37b", "BEVERAGES", ["craft beer", "chilled beer", "bottled beer"]),
        ("wine_glass", "\U0001f377", "BEVERAGES", ["red wine", "white wine", "sula", "grape wine"]),
        ("champagne", "\U0001f37e", "BEVERAGES", ["sparkling drink", "champagne", "celebration drink", "fizzy wine"]),
        ("cocktail", "\U0001f378", "BEVERAGES", ["mocktail", "fruit punch", "mixed juice", "syrup drink"]),
        ("tropical_drink", "\U0001f379", "BEVERAGES", ["lemonade", "nimbu soda", "rooh afza", "rasna", "glucon-d", "tang", "sharbat"]),
        ("ice_cube", "\U0001f9ca", "BEVERAGES", ["ice pack", "mineral water", "drinking water", "bisleri", "kinley", "aquafina", "bailley"]),

        # ==========================================
        # 10. MEAT & SEAFOOD (15 items)
        # ==========================================
        ("poultry_leg", "\U0001f357", "MEAT_FISH", ["chicken", "चिकन", "சிக்கன்", "kozhi", "murgh", "chikken", "curry cut", "chicken curry cut", "chicken drumstick", "chicken breast", "boneless chicken", "broiler"]),
        ("cut_of_meat", "\U0001f969", "MEAT_FISH", ["mutton", "मटन", "gosht", "aattu iraichi", "goat meat", "lamb", "buff", "pork", "beef", "keema", "minced meat"]),
        ("bacon", "\U0001f953", "MEAT_FISH", ["cured meat", "ham", "sausage", "salami"]),
        ("fish", "\U0001f41f", "MEAT_FISH", ["machli", "मछली", "மீன்", "meen", "rohu", "katla", "hilsa", "pomfret", "surmai", "seer fish", "salmon", "fresh fish"]),
        ("fish_cake", "\U0001f365", "MEAT_FISH", ["fish fingers", "fish cutlet", "fish fillet"]),
        ("shrimp", "\U0001f990", "MEAT_FISH", ["prawns", "झींगा", "இறால்", "jheenga", "eraal", "tiger prawns", "white prawns", "cleaned prawns"]),
        ("crab", "\U0001f980", "MEAT_FISH", ["kankda", "நண்டு", "nandu", "fresh crab", "mud crab"]),
        ("lobster", "\U0001f99e", "MEAT_FISH", ["king lobster", "crayfish", "seafood"]),
        ("squid", "\U0001f991", "MEAT_FISH", ["calamari", "kanava", "octopus", "squid rings"]),
        ("oyster", "\U0001f9aa", "MEAT_FISH", ["clams", "mussels", "shellfish"]),
        ("bone", "\U0001f9b4", "MEAT_FISH", ["soup bone", "marrow bone", "meat bones"]),
        ("chicken_whole", "\U0001f414", "MEAT_FISH", ["whole chicken", "desi murgi", "country chicken", "naatu kozhi"]),

        # ==========================================
        # 11. HOUSEHOLD & CLEANING (15 items)
        # ==========================================
        ("broom", "\U0001f9f9", "CLEANING", ["jhadu", "झाड़ू", "துடைப்பம்", "thodappam", "phool jhadu", "grass broom", "floor broom", "wiper", "mop"]),
        ("soap_bar", "\U0001f9fc", "CLEANING", ["soap", "sabun", "साबुन", "சோப்பு", "surf excel", "detergent", "washing powder", "vim bar", "rin soap", "surf excel bar", "dishwash bar", "detergent cake", "exo bar"]),
        ("lotion_bottle", "\U0001f9f4", "CLEANING", ["liquid detergent", "surf excel liquid", "ariel liquid", "lizol", "harpic", "colin", "vim liquid", "comfort", "floor cleaner", "toilet cleaner", "dettol liquid"]),
        ("sponge", "\U0001f9fd", "CLEANING", ["scrub pad", "scotch brite", "dishwash sponge", "steel scrubber", "cleaning scrub", "foam pad"]),
        ("bucket", "\U0001faa3", "CLEANING", ["balti", "बाल्टी", "plastic bucket", "mug", "cleaning pail", "water bucket"]),
        ("toilet_paper", "\U0001f9fb", "CLEANING", ["tissue paper", "kitchen roll", "toilet roll", "paper napkin", "serviette", "face tissue", "origami"]),
        ("wastebasket", "\U0001f5d1", "CLEANING", ["dustbin", "garbage bags", "trash bags", "waste bin"]),
        ("plunger", "\U0001fa9f", "CLEANING", ["drain cleaner", "sink cleaner", "toilet plunger"]),
        ("fire_extinguisher", "\U0001f9ef", "CLEANING", ["fire safety", "extinguisher cylinder"]),

        # ==========================================
        # 12. PERSONAL CARE & HYGIENE (20 items)
        # ==========================================
        ("toothbrush", "\U0001faa5", "PERSONAL_CARE", ["colgate brush", "oral b", "toothpaste", "colgate", "pepsodent", "close up", "sensodyne", "dant kanti", "dabur red", "brush"]),
        ("razor", "\U0001fa92", "PERSONAL_CARE", ["gillette", "shaving blade", "shaving razor", "shaving foam", "shaving cream", "aftershave", "ustara"]),
        ("comb", "\U0001f487", "PERSONAL_CARE", ["kanghi", "seepu", "hair comb", "hair brush", "hair care"]),
        ("lipstick", "\U0001f484", "PERSONAL_CARE", ["lip balm", "vaseline", "lakme", "maybelline", "cosmetics", "lip gloss", "chapstick"]),
        ("nail_polish", "\U0001f485", "PERSONAL_CARE", ["nail paint", "nail enamel", "nail polish remover", "manicure"]),
        ("mirror", "\U0001fa9e", "PERSONAL_CARE", ["sheesha", "pocket mirror", "shaving mirror"]),
        ("scissors", "\U00002702", "PERSONAL_CARE", ["nail cutter", "hair scissors", "grooming scissor"]),
        ("safety_pin", "\U0001f9f7", "PERSONAL_CARE", ["sanitary pads", "whisper", "stayfree", "sofy", "cotton buds", "ear buds", "cotton roll", "bandage tape"]),
        ("bath_soap", "\U0001f9fc", "PERSONAL_CARE", ["dettol soap", "lifebuoy", "lux", "pears", "cinthol", "santoor", "fiama", "body wash", "handwash", "dettol handwash"]),

        # ==========================================
        # 13. BABY CARE (10 items)
        # ==========================================
        ("baby_face", "\U0001f476", "BABY_CARE", ["pampers", "huggies", "mamy poko pants", "baby diapers", "baby wipes", "johnsons baby", "himalaya baby"]),
        ("teddy_bear", "\U0001f9f8", "BABY_CARE", ["soft toy", "baby toy", "plush toy", "rattle"]),
        ("baby_crib", "\U0001f6cc", "BABY_CARE", ["jhoola", "baby cot", "baby bed", "pram"]),

        # ==========================================
        # 14. PET SUPPLIES (12 items)
        # ==========================================
        ("dog", "\U0001f415", "PET_CARE", ["dog food", "pedigree", "drools", "royal canin", "puppy food", "dog biscuits", "dog treats", "meat up"]),
        ("cat", "\U0001f408", "PET_CARE", ["cat food", "whiskas", "me-o", "purepet", "kitten food", "cat treats"]),
        ("dog_face", "\U0001f436", "PET_CARE", ["pet care", "canine"]),
        ("cat_face", "\U0001f431", "PET_CARE", ["feline", "pet care"]),
        ("paw_prints", "\U0001f43e", "PET_CARE", ["pet grooming", "pet shampoo", "pet accessories", "pet care"]),
        ("bird", "\U0001f426", "PET_CARE", ["bird food", "seeds", "parrot food", "lovebirds food"]),
        ("rabbit", "\U0001f407", "PET_CARE", ["rabbit food", "hay", "bunny pellets"]),
        ("hamster", "\U0001f439", "PET_CARE", ["hamster food", "guinea pig food"]),

        # ==========================================
        # 15. STATIONERY & OFFICE (20 items)
        # ==========================================
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
        ("abacus", "\U0001f9ee", "STATIONERY", ["abacus", "calculator", "math tool", "counting frame"]),
        ("paintbrush", "\U0001f58c", "STATIONERY", ["art brush", "water color brush", "painting brush"]),

        # ==========================================
        # 16. KITCHENWARE & TABLEWARE (15 items)
        # ==========================================
        ("kitchen_knife", "\U0001f52a", "KITCHENWARE", ["chaku", "knife", "vegetable knife", "chopping knife", "kitchen cutter"]),
        ("fork_and_knife", "\U0001f374", "KITCHENWARE", ["cutlery", "fork", "table knife", "spoon fork set"]),
        ("spoon", "\U0001f944", "KITCHENWARE", ["chamach", "steel spoon", "tablespoon", "teaspoon", "soup spoon", "serving spoon"]),
        ("plate", "\U0001f37d", "KITCHENWARE", ["thali", "dinner plate", "steel plate", "melamine plate", "quarter plate"]),
        ("cooking_pot", "\U0001f372", "KITCHENWARE", ["patila", "bhagona", "degchi", "cooker", "pressure cooker", "saucepan", "milk pan", "handi"]),
        ("frying_pan", "\U0001f373", "KITCHENWARE", ["kadhai", "tawa", "dosa tawa", "roti tawa", "fry pan", "non stick pan", "prestige", "hawkins"]),
        ("chopsticks", "\U0001f962", "KITCHENWARE", ["wooden chopsticks", "noodle sticks"]),
        ("thermos", "\U0001f376", "KITCHENWARE", ["milton flask", "thermos flask", "hot water bottle", "insulated flask", "water bottle", "school bottle"]),

        # ==========================================
        # 17. ELECTRONICS & ELECTRICALS (18 items)
        # ==========================================
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
        ("television", "\U0001f4fa", "ELECTRONICS", ["tv", "led screen", "television set"]),
        ("radio", "\U0001f4fb", "ELECTRONICS", ["transistor", "fm radio"]),
        ("camera", "\U0001f4f7", "ELECTRONICS", ["dslr", "digital camera"]),

        # ==========================================
        # 18. MEDICINE & HEALTH (15 items)
        # ==========================================
        ("pill", "\U0001f48a", "HEALTH", ["tablet", "capsule", "paracetamol", "crocin", "dolo 650", "pain killer", "saridon", "disprin", "combiflam", "gelusil", "pudina hara", "eno", "vitamin c", "becosules"]),
        ("bandage", "\U0001fa79", "HEALTH", ["band aid", "plaster", "hansaplast", "crepe bandage", "first aid tape"]),
        ("syringe", "\U0001f489", "HEALTH", ["dispo van", "injection", "insulin syringe"]),
        ("stethoscope", "\U0001fa7a", "HEALTH", ["bp monitor", "blood pressure check", "health tracker"]),
        ("thermometer", "\U0001f321", "HEALTH", ["digital thermometer", "fever check", "temperature"]),
        ("face_mask", "\U0001f637", "HEALTH", ["surgical mask", "n95 mask", "cotton mask", "protective mask"]),
        ("first_aid", "\U000026d1", "HEALTH", ["first aid kit", "burnol", "savlon", "betadine", "moov", "iodex", "volini", "fast relief", "vicks vaporub", "amrutanjan"]),

        # ==========================================
        # 19. TOYS, GIFTS & PUJA (20 items)
        # ==========================================
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
        ("sparkler", "\U0001f387", "TOYS_GIFTS", ["fuljhadi", "sparklers", "sparkler stick"]),
        ("greeting_card", "\U0001f48c", "TOYS_GIFTS", ["birthday card", "greeting card", "invitation card"]),

        # ==========================================
        # 20. APPAREL & GENERAL RETAIL (25 items)
        # ==========================================
        ("t_shirt", "\U0001f455", "GENERAL", ["t shirt", "tee", "cotton t shirt", "banian", "vest", "round neck", "polo"]),
        ("jeans", "\U0001f456", "GENERAL", ["denim", "trousers", "pants", "cotton pants"]),
        ("dress", "\U0001f457", "GENERAL", ["frock", "kurti", "ladies wear", "salwar"]),
        ("socks", "\U0001f9e6", "GENERAL", ["cotton socks", "school socks", "ankle socks"]),
        ("shoe", "\U0001f45e", "GENERAL", ["formal shoe", "school shoe", "leather shoes"]),
        ("sneaker", "\U0001f45f", "GENERAL", ["sports shoe", "running shoe", "canvas shoes"]),
        ("sandal", "\U0001f461", "GENERAL", ["slippers", "chappal", "flip flop", "hawai chappal"]),
        ("hat", "\U0001f9e2", "GENERAL", ["cap", "sun cap", "cricket cap"]),
        ("sunglasses", "\U0001f576", "GENERAL", ["goggles", "shades", "sun glasses"]),
        ("glasses", "\U0001f453", "GENERAL", ["spectacles", "chashma", "reading glasses"]),
        ("umbrella", "\U00002602", "GENERAL", ["chatri", "rain umbrella", "folding umbrella"]),
        ("handbag", "\U0001f45c", "GENERAL", ["purse", "shoulder bag", "ladies bag", "wallet"]),
        ("shopping_cart", "\U0001f6d2", "GENERAL", ["supermarket cart", "trolley", "grocery cart", "dukaan cart"]),
        ("shopping_bags", "\U0001f6cd", "GENERAL", ["carry bag", "cloth bag", "paper bag", "jute bag", "shopping bag"]),
        ("package", "\U0001f4e6", "GENERAL", ["parcel", "carton", "box", "delivery box", "courier pack"]),
        ("storefront", "\U0001f3ea", "GENERAL", ["dukaan", "shop", "kirana", "supermarket", "store", "retail"]),
        ("price_tag", "\U0001f3f7", "GENERAL", ["label", "barcode tag", "offer tag", "discount label", "price sticker"]),
        ("receipt", "\U0001f9fe", "GENERAL", ["bill", "cash receipt", "invoice", "payment slip"]),
        ("coin", "\U0001fa99", "GENERAL", ["rupee", "change", "sikka", "currency"]),
        ("banknote", "\U0001f4b5", "GENERAL", ["cash", "rupee note", "currency note", "money"]),
        ("shopping_basket", "\U0001f9fa", "GENERAL", ["tokri", "plastic basket", "hand basket"]),
        ("delivery_truck", "\U0001f69a", "GENERAL", ["home delivery", "tempo", "transport", "logistics"]),
        ("thread", "\U0001f9f5", "GENERAL", ["dhaga", "sewing thread", "reel"]),
        ("yarn", "\U0001f9f6", "GENERAL", ["wool", "knitting yarn", "una"])
    ]

    ADDITIONAL_RETAIL = [
        ("wrench", "\U0001f527", "ELECTRONICS", ["pana", "spanner", "wrench", "hardware"]),
        ("nut_and_bolt", "\U0001f529", "ELECTRONICS", ["bolt", "nut", "screw", "washer"]),
        ("gear", "\U00002699", "ELECTRONICS", ["spare part", "machinery", "cog"]),
        ("mouse", "\U0001f5b1", "ELECTRONICS", ["computer mouse", "optical mouse", "wireless mouse"]),
        ("printer", "\U0001f5a8", "ELECTRONICS", ["bill printer", "printer", "paper printer"]),
        ("monitor", "\U0001f5a5", "ELECTRONICS", ["desktop", "computer screen", "monitor"]),
        ("keyboard", "\U00002328", "ELECTRONICS", ["typing keyboard", "usb keyboard"]),
        ("satellite_antenna", "\U0001f4e1", "ELECTRONICS", ["dish antenna", "dth", "tata play"]),
        ("document_page", "\U0001f4c4", "STATIONERY", ["a4 paper", "print paper", "xerox paper", "paper sheet"]),
        ("bar_chart", "\U0001f4ca", "STATIONERY", ["accounts", "sales chart", "ledger"]),
        ("bookmark", "\U0001f516", "STATIONERY", ["ribbon marker", "page marker"]),
        ("magnifying_glass", "\U0001f50d", "STATIONERY", ["lens", "magnifier", "reading glass"]),
        ("scroll", "\U0001f4dc", "STATIONERY", ["parchment", "certificate", "roll"]),
        ("briefcase", "\U0001f4bc", "STATIONERY", ["office bag", "attache", "document briefcase"]),
        ("postal_horn", "\U0001f4ef", "STATIONERY", ["post", "courier", "speed post"]),
        ("incoming_envelope", "\U0001f4e8", "STATIONERY", ["mail", "letter", "dak"]),
        ("outbox_tray", "\U0001f4e4", "STATIONERY", ["paper tray", "desk organizer"]),
        ("inbox_tray", "\U0001f4e5", "STATIONERY", ["in tray", "document tray"]),
        ("matches_fire", "\U0001f525", "TOYS_GIFTS", ["maachis", "matchbox", "matchstick", "safety matches"]),
        ("sparkles", "\U00002728", "TOYS_GIFTS", ["glitter", "sparkle", "festive"]),
        ("holiday_tree", "\U0001f384", "TOYS_GIFTS", ["christmas tree", "festival tree", "decoration"]),
        ("jack_o_lantern", "\U0001f383", "TOYS_GIFTS", ["pumpkin", "halloween"]),
        ("red_envelope", "\U0001f9e7", "TOYS_GIFTS", ["shagun packet", "cash envelope", "lifafa"]),
        ("firecracker_roll", "\U0001f9e8", "TOYS_GIFTS", ["ladi", "diwali patakha", "crackers"]),
        ("party_hat", "\U0001f389", "TOYS_GIFTS", ["birthday cap", "party hat"]),
        ("crystal_ball", "\U0001f52e", "TOYS_GIFTS", ["magic ball", "toy"]),
        ("magic_wand", "\U0001fa84", "TOYS_GIFTS", ["toy wand", "fairy stick"]),
        ("teddy_toy", "\U0001f9f8", "TOYS_GIFTS", ["plushie", "stuffed animal"]),
        ("kitchen_pot", "\U0001f372", "KITCHENWARE", ["handi", "stew pot", "soup pot"]),
        ("frying_pan_shallow", "\U0001f373", "KITCHENWARE", ["omelette pan", "skillet", "tawa"]),
        ("tea_kettle", "\U0001fad6", "KITCHENWARE", ["water kettle", "tea pot", "chai kettle"]),
        ("katori_bowl", "\U0001f963", "KITCHENWARE", ["soup bowl", "cereal bowl", "steel katori"]),
        ("cutlery_set", "\U0001f374", "KITCHENWARE", ["spoon and fork", "eating utensils"]),
        ("disposable_cup", "\U0001f964", "KITCHENWARE", ["paper cup", "chai glass", "disposable glass"]),
        ("shaving_blade", "\U0001fa92", "PERSONAL_CARE", ["gillette mach 3", "shaving cartridge"]),
        ("cotton_swab", "\U0001f9f7", "PERSONAL_CARE", ["q tips", "ear cleaning buds"]),
        ("perfume_drop", "\U0001f4a7", "PERSONAL_CARE", ["attar", "fragrance", "cologne"]),
        ("eye_glasses", "\U0001f453", "PERSONAL_CARE", ["spectacle frame", "zero power"]),
        ("sun_glasses", "\U0001f576", "PERSONAL_CARE", ["uv sunglasses", "polaroid"]),
        ("footwear_chappal", "\U0001f461", "PERSONAL_CARE", ["bata chappal", "paragon", "relaxo"]),
        ("adhesive_tape", "\U0001fa79", "HEALTH", ["micropore", "medical tape", "cotton gauze"]),
        ("dropper", "\U0001fa78", "HEALTH", ["eye drops", "nasal drops", "ear drops"]),
        ("stethoscope_tool", "\U0001fa7a", "HEALTH", ["health check", "heart monitor"]),
        ("fever_thermometer", "\U0001f321", "HEALTH", ["mercury thermometer", "fever meter"]),
        ("blood_drop", "\U0001fa78", "HEALTH", ["blood test", "glucometer strips", "sugar check"]),
        ("tea_toast", "\U0001f35e", "BAKERY", ["rusk", "suji rusk", "britannia toastea", "milk toast", "cake rusk"]),
        ("pav_bun", "\U0001f354", "BAKERY", ["vada pav bun", "pav bhaji bun", "dinner rolls"]),
        ("paratha_bread", "\U0001fad3", "BAKERY", ["malabar parotta", "frozen paratha", "laccha paratha"]),
        ("nacho_chips", "\U0001f32e", "SNACKS", ["cheese nachos", "corn crisps", "tortilla"]),
        ("kathi_roll", "\U0001f32f", "SNACKS", ["paneer roll", "chicken roll", "egg roll"]),
        ("tea_biscuit", "\U0001f36a", "SNACKS", ["chai biscuit", "tea snack", "rusk"]),
        ("sev_bhujia", "\U0001f963", "SNACKS", ["bhujia", "sev", "भुजिया", "सेव", "namkeen", "नमकीन", "haldiram bhujia", "bikanervala", "ratlami sev", "mixture namkeen", "chivda"]),
        ("salted_peanuts", "\U0001f95c", "SNACKS", ["haldiram salted peanuts", "roasted chana", "snack nuts"]),
        ("mango_frooti", "\U0001f964", "BEVERAGES", ["frooti", "maaza", "mango drink", "paper boat"]),
        ("cola_soft_drink", "\U0001f964", "BEVERAGES", ["cold drink", "soda", "carbonated drink"]),
        ("energy_drink_can", "\U0001f964", "BEVERAGES", ["red bull", "sting energy", "monster"]),
        ("mineral_water_pack", "\U0001f9ca", "BEVERAGES", ["packaged water", "20l jar", "water bottle"]),
        ("badam_milk", "\U0001f95b", "BEVERAGES", ["almond milk", "kesar milk", "amul kool"]),
        ("chaas_buttermilk", "\U0001f95b", "BEVERAGES", ["masala chaas", "amul masti chaas", "mattha", "moru"]),
        ("paneer_pack", "\U0001f9c0", "DAIRY", ["fresh paneer", "malai paneer", "amul paneer", "gowardhan paneer"]),
        ("ghee_jar", "\U0001f9c8", "DAIRY", ["amul ghee", "cow ghee", "shudh desi ghee", "patanjali ghee", "aashirvaad svasti ghee", "mother dairy ghee", "grb ghee"])
    ]

    all_raw = ITEMS + ADDITIONAL_RETAIL
    print(f"Total defined retail items: {len(all_raw)}")

    items_by_key = {}
    for key, char, dept, kw in all_raw:
        if key not in items_by_key:
            items_by_key[key] = (key, char, dept, kw)
        else:
            k, c, d, existing_kw = items_by_key[key]
            merged_kw = list(dict.fromkeys(existing_kw + kw))
            items_by_key[key] = (k, c, d, merged_kw)

    unique_items = list(items_by_key.values())
    print(f"Total unique item keys: {len(unique_items)}")

    valid_items = []
    for key, char, dept, kw in unique_items:
        cp = '_'.join(f'{ord(c):x}' for c in char)
        fname = f'emoji_u{cp}.svg'
        if fname in svg_cache:
            valid_items.append((key, char, cp, fname, dept, kw))
        else:
            print(f"WARNING: Skipping {key} because {fname} not found in Noto Emoji repo")

    print(f"Total valid SVG items ready to compile: {len(valid_items)}")
    with open('tools/compiled_items_list.json', 'w', encoding='utf-8') as f:
        json.dump(valid_items, f, ensure_ascii=False, indent=2)

if __name__ == '__main__':
    main()