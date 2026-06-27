package com.example.data

object Localization {
    private val translations = mapOf(
        "StudyTZ" to mapOf(
            "Kiswahili" to "StudyTZ",
            "English" to "StudyTZ",
            "France" to "StudyTZ",
            "Kichina" to "StudyTZ"
        ),
        "Chuo Kiganjani Mwako" to mapOf(
            "Kiswahili" to "Chuo Kiganjani Mwako",
            "English" to "Your University in your Pocket",
            "France" to "Votre université dans votre poche",
            "Kichina" to "口袋里的大学"
        ),
        "Namba ya Simu" to mapOf(
            "Kiswahili" to "Namba ya Simu",
            "English" to "Phone Number",
            "France" to "Numéro de téléphone",
            "Kichina" to "电话号码"
        ),
        "Neno la Siri" to mapOf(
            "Kiswahili" to "Neno la Siri",
            "English" to "Password",
            "France" to "Mot de passe",
            "Kichina" to "密码"
        ),
        "Ingia" to mapOf(
            "Kiswahili" to "Ingia",
            "English" to "Login",
            "France" to "Connexion",
            "Kichina" to "登录"
        ),
        "Huna Akaunti? Jisajili" to mapOf(
            "Kiswahili" to "Huna Akaunti? Jisajili",
            "English" to "Don't have an account? Register",
            "France" to "Pas de compte? S'inscrire",
            "Kichina" to "没有账号？注册"
        ),
        "Tengeneza Akaunti" to mapOf(
            "Kiswahili" to "Tengeneza Akaunti",
            "English" to "Create Account",
            "France" to "Créer un compte",
            "Kichina" to "创建账户"
        ),
        "Jina Kamili" to mapOf(
            "Kiswahili" to "Jina Kamili",
            "English" to "Full Name",
            "France" to "Nom complet",
            "Kichina" to "姓名"
        ),
        "Chuo Kikuu" to mapOf(
            "Kiswahili" to "Chuo Kikuu",
            "English" to "University",
            "France" to "Université",
            "Kichina" to "大学"
        ),
        "Kampasi" to mapOf(
            "Kiswahili" to "Kampasi",
            "English" to "Campus",
            "France" to "Campus",
            "Kichina" to "校区"
        ),
        "Kitivo" to mapOf(
            "Kiswahili" to "Kitivo",
            "English" to "Faculty",
            "France" to "Faculté",
            "Kichina" to "学院"
        ),
        "Idara" to mapOf(
            "Kiswahili" to "Idara",
            "English" to "Department",
            "France" to "Département",
            "Kichina" to "专业/部门"
        ),
        "Kozi" to mapOf(
            "Kiswahili" to "Kozi",
            "English" to "Programme/Course",
            "France" to "Programme d'études",
            "Kichina" to "学位课程"
        ),
        "Mwaka wa Masomo" to mapOf(
            "Kiswahili" to "Mwaka wa Masomo",
            "English" to "Year of Study",
            "France" to "Année d'étude",
            "Kichina" to "学年"
        ),
        "Jisajili" to mapOf(
            "Kiswahili" to "Jisajili",
            "English" to "Register",
            "France" to "S'inscrire",
            "Kichina" to "注册"
        ),
        "Umeshajiunga? Ingia" to mapOf(
            "Kiswahili" to "Umeshajiunga? Ingia",
            "English" to "Already have an account? Login",
            "France" to "Déjà membre? Connexion",
            "Kichina" to "已有账户？登录"
        ),
        "Gundua & Shiriki" to mapOf(
            "Kiswahili" to "Gundua & Shiriki",
            "English" to "Discover & Share",
            "France" to "Découvrir et partager",
            "Kichina" to "发现与分享"
        ),
        "Tafuta machapisho, washiriki, au vyuo..." to mapOf(
            "Kiswahili" to "Tafuta machapisho, washiriki, au vyuo...",
            "English" to "Search posts, buddies, or universities...",
            "France" to "Rechercher des posts, amis, ou universités...",
            "Kichina" to "搜索帖子、伙伴或大学..."
        ),
        "Nyumbani" to mapOf(
            "Kiswahili" to "Nyumbani",
            "English" to "Home",
            "France" to "Accueil",
            "Kichina" to "首页"
        ),
        "Video za Masomo" to mapOf(
            "Kiswahili" to "Video za Masomo",
            "English" to "Study Reels",
            "France" to "Vidéos éducatives",
            "Kichina" to "学习短视频"
        ),
        "Marafiki wa Masomo" to mapOf(
            "Kiswahili" to "Marafiki wa Masomo",
            "English" to "Study Buddies",
            "France" to "Partenaires d'étude",
            "Kichina" to "学习伙伴"
        ),
        "Sura na AI" to mapOf(
            "Kiswahili" to "StudyAI Chat",
            "English" to "StudyAI Chat",
            "France" to "Chat StudyAI",
            "Kichina" to "AI 学习助手"
        ),
        "Mipangilio" to mapOf(
            "Kiswahili" to "Mipangilio",
            "English" to "Settings",
            "France" to "Paramètres",
            "Kichina" to "设置"
        ),
        "Hariri Wasifu" to mapOf(
            "Kiswahili" to "Hariri Wasifu",
            "English" to "Edit Profile",
            "France" to "Modifier le profil",
            "Kichina" to "编辑资料"
        ),
        "Modi ya Usiku" to mapOf(
            "Kiswahili" to "Modi ya Usiku",
            "English" to "Dark Theme",
            "France" to "Thème sombre",
            "Kichina" to "深色模式"
        ),
        "Lugha" to mapOf(
            "Kiswahili" to "Lugha",
            "English" to "Language",
            "France" to "Langue",
            "Kichina" to "语言"
        ),
        "Ondoka Kwenye Mfumo" to mapOf(
            "Kiswahili" to "Ondoka Kwenye Mfumo",
            "English" to "Logout",
            "France" to "Se déconnecter",
            "Kichina" to "退出登录"
        ),
        "Andika Chapisho..." to mapOf(
            "Kiswahili" to "Andika Chapisho...",
            "English" to "Write a post...",
            "France" to "Écrire un post...",
            "Kichina" to "写下分享..."
        ),
        "Maswali ya Maoni" to mapOf(
            "Kiswahili" to "Weka Kura",
            "English" to "Create a Poll",
            "France" to "Créer un sondage",
            "Kichina" to "发起投票"
        ),
        "Maoni" to mapOf(
            "Kiswahili" to "Maoni",
            "English" to "Comments",
            "France" to "Commentaires",
            "Kichina" to "评论"
        ),
        "Weka Maoni..." to mapOf(
            "Kiswahili" to "Weka Maoni...",
            "English" to "Add a comment...",
            "France" to "Ajouter un commentaire...",
            "Kichina" to "添加评论..."
        ),
        "Tuma" to mapOf(
            "Kiswahili" to "Tuma",
            "English" to "Post",
            "France" to "Publier",
            "Kichina" to "发布"
        ),
        "Picha" to mapOf(
            "Kiswahili" to "Picha",
            "English" to "Image",
            "France" to "Image",
            "Kichina" to "图片"
        ),
        "Ulinzi na 2FA upo hai" to mapOf(
            "Kiswahili" to "Ulinzi na 2FA upo hai kikamilifu!",
            "English" to "Security & 2FA is fully active!",
            "France" to "La sécurité et la 2FA sont pleinement actives!",
            "Kichina" to "安全与双重认证已全面激活！"
        ),
        "Kuhifadhi data kumewezeshwa" to mapOf(
            "Kiswahili" to "Kuhifadhi data (Cloud Storage) kumewezeshwa!",
            "English" to "Data Backup (Cloud Storage) is enabled!",
            "France" to "La sauvegarde des données (Cloud) est activée!",
            "Kichina" to "云端数据备份已启用！"
        ),
        "Msaada unapatikana 24/7" to mapOf(
            "Kiswahili" to "Msaada unapatikana 24/7 kwa simu ya bodi na vyuo!",
            "English" to "Support is available 24/7 for board and colleges!",
            "France" to "Le support est disponible 24/7 pour le conseil et les collèges!",
            "Kichina" to "板卡和高校支持服务 24/7 全天候在线！"
        ),
        "Hifadhi Wasifu" to mapOf(
            "Kiswahili" to "Hifadhi Wasifu",
            "English" to "Save Profile",
            "France" to "Enregistrer le profil",
            "Kichina" to "保存个人资料"
        ),
        "Karibu StudyTZ!" to mapOf(
            "Kiswahili" to "Karibu StudyTZ!",
            "English" to "Welcome to StudyTZ!",
            "France" to "Bienvenue sur StudyTZ!",
            "Kichina" to "欢迎来到 StudyTZ！"
        ),
        "Arifa" to mapOf(
            "Kiswahili" to "Arifa",
            "English" to "Notifications",
            "France" to "Notifications",
            "Kichina" to "通知"
        ),
        "Tafuta" to mapOf(
            "Kiswahili" to "Tafuta",
            "English" to "Search",
            "France" to "Rechercher",
            "Kichina" to "搜索"
        ),
        "Funga utafutaji" to mapOf(
            "Kiswahili" to "Funga utafutaji",
            "English" to "Close search",
            "France" to "Fermer la recherche",
            "Kichina" to "关闭搜索"
        ),
        "Kazi za Masomo" to mapOf(
            "Kiswahili" to "Kazi za Masomo",
            "English" to "Academic Materials",
            "France" to "Matériel académique",
            "Kichina" to "学术资料"
        ),
        "Maktaba ya Rasilimali" to mapOf(
            "Kiswahili" to "Maktaba ya Rasilimali",
            "English" to "Resource Library",
            "France" to "Bibliothèque de ressources",
            "Kichina" to "资源库"
        ),
        "Pakia Nyaraka" to mapOf(
            "Kiswahili" to "Pakia Nyaraka",
            "English" to "Upload Document",
            "France" to "Téléverser un document",
            "Kichina" to "上传文件"
        ),
        "Piga picha ya maktaba" to mapOf(
            "Kiswahili" to "Piga picha ya maktaba au slides ili uziunde kwa AI",
            "English" to "Take photo of library or slides to analyze with AI",
            "France" to "Prendre en photo la bibliothèque ou slides pour l'IA",
            "Kichina" to "拍摄图书馆或幻灯片照片以供 AI 分析"
        ),
        "Marafiki wa Kujifunza" to mapOf(
            "Kiswahili" to "Marafiki wa Kujifunza",
            "English" to "Study Partners",
            "France" to "Partenaires d'étude",
            "Kichina" to "学习同伴"
        ),
        "Vikundi vya Masomo" to mapOf(
            "Kiswahili" to "Vikundi vya Masomo",
            "English" to "Study Groups",
            "France" to "Groupes d'étude",
            "Kichina" to "学习小组"
        ),
        "Ujumbe wa siri" to mapOf(
            "Kiswahili" to "Ujumbe wa siri",
            "English" to "Direct Message",
            "France" to "Message direct",
            "Kichina" to "私信"
        ),
        "Lugha iliyochaguliwa ni" to mapOf(
            "Kiswahili" to "Lugha iliyochaguliwa ni Kiswahili",
            "English" to "Selected language is English",
            "France" to "La langue sélectionnée est le français",
            "Kichina" to "已选择语言：中文"
        ),
        "Tuma ujumbe kwa AI..." to mapOf(
            "Kiswahili" to "Tuma ujumbe kwa AI...",
            "English" to "Ask StudyAI anything...",
            "France" to "Demander à StudyAI...",
            "Kichina" to "向 StudyAI 提问..."
        ),
        "Namba au Neno la siri limekosewa!" to mapOf(
            "Kiswahili" to "Namba au Neno la siri limekosewa!",
            "English" to "Incorrect phone number or password!",
            "France" to "Numéro de téléphone ou mot de passe incorrect!",
            "Kichina" to "电话号码或密码不正确！"
        ),
        "Usajili umekamilika!" to mapOf(
            "Kiswahili" to "Usajili umekamilika!",
            "English" to "Registration complete!",
            "France" to "Inscription terminée!",
            "Kichina" to "注册已完成！"
        ),
        "Hariri Wasifu Kamili" to mapOf(
            "Kiswahili" to "Hariri Wasifu Kamili",
            "English" to "Edit Full Profile",
            "France" to "Modifier le profil complet",
            "Kichina" to "编辑完整资料"
        ),
        "Jina" to mapOf(
            "Kiswahili" to "Jina",
            "English" to "Name",
            "France" to "Nom",
            "Kichina" to "姓名"
        ),
        "Chuo" to mapOf(
            "Kiswahili" to "Chuo",
            "English" to "College / Uni",
            "France" to "Université / Collège",
            "Kichina" to "大学/学院"
        ),
        "Bio" to mapOf(
            "Kiswahili" to "Bio",
            "English" to "Bio",
            "France" to "Bio",
            "Kichina" to "个人简介"
        ),
        "Nchi" to mapOf(
            "Kiswahili" to "Nchi",
            "English" to "Country",
            "France" to "Pays",
            "Kichina" to "国家"
        ),
        "Idara" to mapOf(
            "Kiswahili" to "Idara",
            "English" to "Department",
            "France" to "Département",
            "Kichina" to "部门"
        ),
        "Kozi" to mapOf(
            "Kiswahili" to "Kozi",
            "English" to "Course",
            "France" to "Cours",
            "Kichina" to "课程"
        ),
        "Mipangilio ya Mfumo" to mapOf(
            "Kiswahili" to "Mipangilio ya Mfumo",
            "English" to "System Settings",
            "France" to "Paramètres du système",
            "Kichina" to "系统设置"
        ),
        "Nchini" to mapOf(
            "Kiswahili" to "Nchi",
            "English" to "Country",
            "France" to "Pays",
            "Kichina" to "国家"
        ),
        "Simu" to mapOf(
            "Kiswahili" to "Simu",
            "English" to "Phone",
            "France" to "Téléphone",
            "Kichina" to "电话"
        ),
        "Wafuasi" to mapOf(
            "Kiswahili" to "Wafuasi",
            "English" to "Followers",
            "France" to "Abonnés",
            "Kichina" to "粉丝"
        ),
        "Machapisho" to mapOf(
            "Kiswahili" to "Machapisho",
            "English" to "Posts",
            "France" to "Publications",
            "Kichina" to "帖子"
        ),
        "Ondoka" to mapOf(
            "Kiswahili" to "Ondoka",
            "English" to "Logout",
            "France" to "Se déconnecter",
            "Kichina" to "退出"
        ),
        "Tafuta marafiki wa masomo..." to mapOf(
            "Kiswahili" to "Tafuta marafiki wa masomo...",
            "English" to "Search study partners...",
            "France" to "Rechercher des partenaires...",
            "Kichina" to "搜索学习同伴..."
        ),
        "Soma Sasa" to mapOf(
            "Kiswahili" to "Soma Sasa",
            "English" to "Read Now",
            "France" to "Lire maintenant",
            "Kichina" to "现在学习"
        ),
        "Bodi ya Mikopo (HESLB) Updates" to mapOf(
            "Kiswahili" to "Taarifa za Bodi ya Mikopo (HESLB)",
            "English" to "Higher Education Loans Board (HESLB)",
            "France" to "Mises à jour des bourses (HESLB)",
            "Kichina" to "高等教育贷款委员会 (HESLB) 更新"
        ),
        "Unda Status" to mapOf(
            "Kiswahili" to "Unda Status",
            "English" to "Create Status",
            "France" to "Créer un statut",
            "Kichina" to "创建状态"
        ),
        "Hakuna machapisho yanayofanana" to mapOf(
            "Kiswahili" to "Hakuna machapisho yanayofanana na utafutaji wako.",
            "English" to "No posts match your search.",
            "France" to "Aucune publication ne correspond à votre recherche.",
            "Kichina" to "没有找到匹配的帖子。"
        ),
        "Safisha" to mapOf(
            "Kiswahili" to "Safisha",
            "English" to "Clear",
            "France" to "Effacer",
            "Kichina" to "清除"
        ),
        "Weka Status" to mapOf(
            "Kiswahili" to "Weka Status",
            "English" to "Add Status",
            "France" to "Ajouter un statut",
            "Kichina" to "添加状态"
        ),
        "Kura yako kwa" to mapOf(
            "Kiswahili" to "Kura yako kwa '%s' imehesabiwa!",
            "English" to "Your vote for '%s' has been counted!",
            "France" to "Votre vote pour '%s' a été pris en compte!",
            "Kichina" to "您对 '%s' 的投票已计入！"
        ),
        "Imehifadhiwa!" to mapOf(
            "Kiswahili" to "Imehifadhiwa!",
            "English" to "Saved!",
            "France" to "Enregistré!",
            "Kichina" to "已保存！"
        ),
        "Umeondoa maktaba!" to mapOf(
            "Kiswahili" to "Umeondoa maktaba!",
            "English" to "Removed from library!",
            "France" to "Retiré de la bibliothèque!",
            "Kichina" to "已从库中移除！"
        ),
        "Maoni & Majibu" to mapOf(
            "Kiswahili" to "Maoni & Majibu",
            "English" to "Comments & Replies",
            "France" to "Commentaires & Réponses",
            "Kichina" to "评论与回复"
        ),
        "Funga" to mapOf(
            "Kiswahili" to "Funga",
            "English" to "Close",
            "France" to "Fermer",
            "Kichina" to "关闭"
        ),
        "Jibu" to mapOf(
            "Kiswahili" to "Jibu",
            "English" to "Reply",
            "France" to "Répondre",
            "Kichina" to "回复"
        ),
        "Unamjibu" to mapOf(
            "Kiswahili" to "Unamjibu",
            "English" to "Replying to",
            "France" to "En réponse à",
            "Kichina" to "正在回复"
        ),
        "Andika jibu hapa..." to mapOf(
            "Kiswahili" to "Andika jibu hapa...",
            "English" to "Write a reply here...",
            "France" to "Écrire une réponse ici...",
            "Kichina" to "在此输入回复..."
        ),
        "Andika maoni hapa..." to mapOf(
            "Kiswahili" to "Andika maoni hapa...",
            "English" to "Write a comment here...",
            "France" to "Écrire un commentaire ici...",
            "Kichina" to "在此输入评论..."
        ),
        "Hakuna reels bado." to mapOf(
            "Kiswahili" to "Hakuna reels bado.",
            "English" to "No reels yet.",
            "France" to "Pas encore de reels.",
            "Kichina" to "暂无短视频。"
        ),
        "Umeipenda Reel hii!" to mapOf(
            "Kiswahili" to "Umeipenda Reel hii!",
            "English" to "You liked this Reel!",
            "France" to "Vous avez aimé ce Reel!",
            "Kichina" to "您点赞了此短视频！"
        ),
        "Umehifadhi kwenye Maktaba!" to mapOf(
            "Kiswahili" to "Umehifadhi kwenye Maktaba!",
            "English" to "Saved to Library!",
            "France" to "Enregistré dans la bibliothèque!",
            "Kichina" to "已保存至库！"
        ),
        "Reel imeondolewa maktaba!" to mapOf(
            "Kiswahili" to "Reel imeondolewa maktaba!",
            "English" to "Reel removed from library!",
            "France" to "Reel retiré de la bibliothèque!",
            "Kichina" to "已从库中移除短视频！"
        ),
        "Kiunganishi cha Reel kimenakiliwa!" to mapOf(
            "Kiswahili" to "Kiunganishi cha Reel kimenakiliwa!",
            "English" to "Reel link copied!",
            "France" to "Lien du Reel copié!",
            "Kichina" to "短视频链接已复制！"
        ),
        "Reels za Chuo" to mapOf(
            "Kiswahili" to "Reels za Chuo",
            "English" to "Campus Reels",
            "France" to "Reels du Campus",
            "Kichina" to "校园短视频"
        ),
        "Unda Reel" to mapOf(
            "Kiswahili" to "Unda Reel",
            "English" to "Create Reel",
            "France" to "Créer un Reel",
            "Kichina" to "创建短视频"
        ),
        "Imehifadhiwa" to mapOf(
            "Kiswahili" to "Imehifadhiwa",
            "English" to "Saved",
            "France" to "Enregistré",
            "Kichina" to "已保存"
        ),
        "Hifadhi" to mapOf(
            "Kiswahili" to "Hifadhi",
            "English" to "Save",
            "France" to "Enregistrer",
            "Kichina" to "保存"
        ),
        "Shiriki" to mapOf(
            "Kiswahili" to "Shiriki",
            "English" to "Share",
            "France" to "Partager",
            "Kichina" to "分享"
        ),
        "Wenzako" to mapOf(
            "Kiswahili" to "Partners",
            "English" to "Partners",
            "France" to "Partenaires",
            "Kichina" to "伙伴"
        ),
        "Vikundi" to mapOf(
            "Kiswahili" to "Vikundi",
            "English" to "Groups",
            "France" to "Groupes",
            "Kichina" to "小组"
        ),
        "Maktaba" to mapOf(
            "Kiswahili" to "Maktaba",
            "English" to "Library",
            "France" to "Bibliothèque",
            "Kichina" to "图书馆"
        ),
        "Malengo" to mapOf(
            "Kiswahili" to "Malengo",
            "English" to "Goals",
            "France" to "Objectifs",
            "Kichina" to "目标"
        ),
        "Tafuta Partners" to mapOf(
            "Kiswahili" to "Tafuta Partners (Mechi ya Chuo)",
            "English" to "Search Partners (Campus Match)",
            "France" to "Rechercher des partenaires (Match Campus)",
            "Kichina" to "寻找伙伴（校园匹配）"
        ),
        "Mechi" to mapOf(
            "Kiswahili" to "Match",
            "English" to "Match",
            "France" to "Match",
            "Kichina" to "匹配"
        ),
        "Mwaka" to mapOf(
            "Kiswahili" to "Mwaka",
            "English" to "Year",
            "France" to "Année",
            "Kichina" to "年份"
        ),
        "Omba Kusoma" to mapOf(
            "Kiswahili" to "Omba Kusoma",
            "English" to "Request Study",
            "France" to "Demander à étudier",
            "Kichina" to "请求共同学习"
        ),
        "Vyumba vya Masomo" to mapOf(
            "Kiswahili" to "Vyumba vya Masomo ya Kikundi",
            "English" to "Group Study Rooms",
            "France" to "Salles d'étude de groupe",
            "Kichina" to "小组学习室"
        ),
        "Kikundi" to mapOf(
            "Kiswahili" to "Kikundi",
            "English" to "Group",
            "France" to "Groupe",
            "Kichina" to "小组"
        ),
        "Wanachama" to mapOf(
            "Kiswahili" to "Wachama",
            "English" to "Members",
            "France" to "Membres",
            "Kichina" to "成员"
        ),
        "Kutana" to mapOf(
            "Kiswahili" to "Kutana",
            "English" to "Meet",
            "France" to "Rejoindre",
            "Kichina" to "见面/进入会议"
        ),
        "Anzisha Kikundi Kipya" to mapOf(
            "Kiswahili" to "Anzisha Kikundi Kipya",
            "English" to "Start New Group",
            "France" to "Créer un nouveau groupe",
            "Kichina" to "创建新小组"
        ),
        "Jina la Kikundi" to mapOf(
            "Kiswahili" to "Jina la Kikundi",
            "English" to "Group Name",
            "France" to "Nom du groupe",
            "Kichina" to "小组名称"
        ),
        "Kozi" to mapOf(
            "Kiswahili" to "Kozi",
            "English" to "Course",
            "France" to "Cours",
            "Kichina" to "课程"
        ),
        "Maelezo ya Kikundi" to mapOf(
            "Kiswahili" to "Maelezo ya Kikundi",
            "English" to "Group Description",
            "France" to "Description du groupe",
            "Kichina" to "小组描述"
        ),
        "Unda Sasa" to mapOf(
            "Kiswahili" to "Unda Sasa",
            "English" to "Create Now",
            "France" to "Créer maintenant",
            "Kichina" to "立即创建"
        ),
        "Pakia PDF" to mapOf(
            "Kiswahili" to "Pakia PDF",
            "English" to "Upload PDF",
            "France" to "Téléverser PDF",
            "Kichina" to "上传 PDF"
        ),
        "Mwalimu" to mapOf(
            "Kiswahili" to "Mwalimu",
            "English" to "Lecturer",
            "France" to "Enseignant",
            "Kichina" to "讲师"
        ),
        "Somo" to mapOf(
            "Kiswahili" to "Somo",
            "English" to "Subject",
            "France" to "Sujet",
            "Kichina" to "科目"
        ),
        "Pakuliwa" to mapOf(
            "Kiswahili" to "Pakuliwa",
            "English" to "Downloads",
            "France" to "Téléchargements",
            "Kichina" to "下载量"
        ),
        "Pakua" to mapOf(
            "Kiswahili" to "Pakua",
            "English" to "Download",
            "France" to "Télécharger",
            "Kichina" to "下载"
        ),
        "PDF imepakuliwa" to mapOf(
            "Kiswahili" to "PDF imepakuliwa kwenye simu yako!",
            "English" to "PDF downloaded to your phone!",
            "France" to "PDF téléchargé sur votre téléphone!",
            "Kichina" to "PDF 已下载到您的手机！"
        ),
        "Pakia faili la Masomo" to mapOf(
            "Kiswahili" to "Pakia faili la Masomo",
            "English" to "Upload Study Material",
            "France" to "Téléverser du matériel d'étude",
            "Kichina" to "上传学习资料"
        ),
        "Jina la PDF au Slide" to mapOf(
            "Kiswahili" to "Jina la PDF au Slide",
            "English" to "PDF or Slide Title",
            "France" to "Titre du PDF ou Diapo",
            "Kichina" to "PDF 或幻灯片标题"
        ),
        "Somo au Kozi" to mapOf(
            "Kiswahili" to "Somo au Kozi",
            "English" to "Subject or Course",
            "France" to "Sujet ou Cours",
            "Kichina" to "科目或课程"
        ),
        "Hifadhi Maktaba" to mapOf(
            "Kiswahili" to "Hifadhi Maktaba",
            "English" to "Save to Library",
            "France" to "Enregistrer dans la bibliothèque",
            "Kichina" to "保存到库"
        ),
        "Kufuatilia Malengo" to mapOf(
            "Kiswahili" to "Chombo cha Kufuatilia Malengo",
            "English" to "Goal Tracker",
            "France" to "Suivi des objectifs",
            "Kichina" to "目标追踪器"
        ),
        "Lengo" to mapOf(
            "Kiswahili" to "Lengo",
            "English" to "Goal",
            "France" to "Objectif",
            "Kichina" to "目标"
        ),
        "Imekamilika" to mapOf(
            "Kiswahili" to "Imekamilika",
            "English" to "Completed",
            "France" to "Terminé",
            "Kichina" to "已完成"
        ),
        "Inaendelea" to mapOf(
            "Kiswahili" to "Inaendelea",
            "English" to "In Progress",
            "France" to "En cours",
            "Kichina" to "进行中"
        ),
        "Masaa" to mapOf(
            "Kiswahili" to "Masaa",
            "English" to "Hours",
            "France" to "Heures",
            "Kichina" to "学时"
        ),
        "Saa" to mapOf(
            "Kiswahili" to "saa",
            "English" to "hour",
            "France" to "heure",
            "Kichina" to "小时"
        ),
        "Sajili Lengo" to mapOf(
            "Kiswahili" to "Sajili Lengo la Kujisomea",
            "English" to "Register Study Goal",
            "France" to "Enregistrer l'objectif d'étude",
            "Kichina" to "登记学习目标"
        ),
        "Lengo Langu" to mapOf(
            "Kiswahili" to "Lengo langu (e.g. Java Network OOP)",
            "English" to "My Goal (e.g. Java Network OOP)",
            "France" to "Mon objectif (ex. Java Network OOP)",
            "Kichina" to "我的目标（例如：Java 网络面向对象）"
        ),
        "Masaa ya Malengo" to mapOf(
            "Kiswahili" to "Masaa ya Malengo",
            "English" to "Target Hours",
            "France" to "Heures cibles",
            "Kichina" to "目标学时"
        ),
        "StudyAI Msaidizi" to mapOf(
            "Kiswahili" to "StudyAI - Msaidizi Wako wa Kitaaluma",
            "English" to "StudyAI - Your Academic Assistant",
            "France" to "StudyAI - Votre assistant académique",
            "Kichina" to "StudyAI - 您的学术助手"
        ),
        "Uulize chochote" to mapOf(
            "Kiswahili" to "Uulize chochote: Summarize, Translate au Quiz!",
            "English" to "Ask anything: Summarize, Translate or Quiz!",
            "France" to "Demandez tout : Résumer, Traduire ou Quiz !",
            "Kichina" to "向我提问：总结、翻译或测验！"
        ),
        "Historia imefutwa" to mapOf(
            "Kiswahili" to "Historia ya mazungumzo imefutwa!",
            "English" to "Chat history cleared!",
            "France" to "Historique des discussions effacé !",
            "Kichina" to "聊天记录已清除！"
        ),
        "Key haijasanidiwa" to mapOf(
            "Kiswahili" to "Gemini API Key haijasanidiwa!",
            "English" to "Gemini API Key is not configured!",
            "France" to "Clé API Gemini non configurée !",
            "Kichina" to "Gemini API 密钥未配置！"
        ),
        "Weka Key" to mapOf(
            "Kiswahili" to "Tafadhali weka GEMINI_API_KEY kwenye Secrets panel ya Google AI Studio ili kuwezesha AI kufanya kazi kikamilifu.",
            "English" to "Please set GEMINI_API_KEY in the Secrets panel of Google AI Studio to enable full AI capability.",
            "France" to "Veuillez configurer GEMINI_API_KEY dans le panneau Secrets de Google AI Studio pour activer l'IA.",
            "Kichina" to "请在 Google AI Studio 的 Secrets 面板中设置 GEMINI_API_KEY 以启用完整的 AI 功能。"
        ),
        "Karibu StudyAI" to mapOf(
            "Kiswahili" to "Karibu StudyAI!",
            "English" to "Welcome to StudyAI!",
            "France" to "Bienvenue sur StudyAI !",
            "Kichina" to "欢迎来到 StudyAI！"
        ),
        "Mimi ni msaidizi" to mapOf(
            "Kiswahili" to "Mimi ni msaidizi wako wa kibinafsi wa masomo. Naweza kukusaidia kufanya muhtasari, kueleza dhana ngumu, kutafsiri, au kutengeneza maswali ya kujipima.",
            "English" to "I am your personal study assistant. I can help you summarize, explain complex concepts, translate, or create practice quizzes.",
            "France" to "Je suis votre assistant d'étude personnel. Je peux vous aider à résumer, expliquer des concepts complexes, traduire ou créer des quiz d'entraînement.",
            "Kichina" to "我是您的个人学习助手。我可以帮助您总结、解释复杂概念、翻译或创建练习测验。"
        ),
        "Mifano" to mapOf(
            "Kiswahili" to "Mifano ya maswali unayoweza kuuliza:",
            "English" to "Examples of questions you can ask:",
            "France" to "Exemples de questions que vous pouvez poser :",
            "Kichina" to "您可以提问的示例："
        ),
        "Fupisha dondoo za somo" to mapOf(
            "Kiswahili" to "Fupisha dondoo za somo la Web Development",
            "English" to "Summarize Web Development study notes",
            "France" to "Résumer les notes d'étude sur le développement Web",
            "Kichina" to "总结网页开发学习笔记"
        ),
        "Tengeneza maswali 5" to mapOf(
            "Kiswahili" to "Tengeneza maswali 5 ya Quiz ya Physics",
            "English" to "Create 5 Physics Quiz questions",
            "France" to "Créer 5 questions de quiz de physique",
            "Kichina" to "创建 5 个物理测验问题"
        ),
        "Tafsiri Explain OOP" to mapOf(
            "Kiswahili" to "Tafsiri: 'Explain Object-Oriented Programming'",
            "English" to "Translate: 'Explain Object-Oriented Programming'",
            "France" to "Traduire : 'Expliquer la programmation orientée objet'",
            "Kichina" to "翻译：“解释面向对象编程”"
        ),
        "StudyAI anachambua" to mapOf(
            "Kiswahili" to "StudyAI anachambua na kuandaa dondoo zako...",
            "English" to "StudyAI is analyzing and preparing your notes...",
            "France" to "StudyAI analyse et prépare vos notes...",
            "Kichina" to "StudyAI 正在分析并准备您的笔记..."
        ),
        "Njia za Haraka" to mapOf(
            "Kiswahili" to "Njia za Haraka (Multi-Intent Shortcuts)",
            "English" to "Quick Shortcuts (Multi-Intent Shortcuts)",
            "France" to "Raccourcis rapides (Raccourcis multi-intentions)",
            "Kichina" to "快捷方式（多功能捷径）"
        ),
        "Fupisha Masomo CHIP" to mapOf(
            "Kiswahili" to "Fupisha Masomo",
            "English" to "Summarize Study",
            "France" to "Résumer l'étude",
            "Kichina" to "总结学习"
        ),
        "Tengeneza Notes CHIP" to mapOf(
            "Kiswahili" to "Tengeneza Notes",
            "English" to "Generate Notes",
            "France" to "Générer des notes",
            "Kichina" to "生成笔记"
        ),
        "Tafsiri CHIP" to mapOf(
            "Kiswahili" to "Tafsiri Kiingereza-Kiswahili",
            "English" to "Translate English-Swahili",
            "France" to "Traduire Anglais-Swahili",
            "Kichina" to "英汉翻译"
        ),
        "Tengeneza Quiz CHIP" to mapOf(
            "Kiswahili" to "Tengeneza Quiz/Maswali",
            "English" to "Generate Quiz/Questions",
            "France" to "Générer un quiz/questions",
            "Kichina" to "生成测验/问题"
        ),
        "Ratiba ya Wiki CHIP" to mapOf(
            "Kiswahili" to "Ratiba ya Wiki",
            "English" to "Weekly Schedule",
            "France" to "Emploi du temps de la semaine",
            "Kichina" to "每周日程表"
        ),
        "Andika hapa placeholder" to mapOf(
            "Kiswahili" to "Andika hapa... (e.g. Fupisha topic ya OOP)",
            "English" to "Write here... (e.g. Summarize OOP topic)",
            "France" to "Écrire ici... (ex. Résumer le sujet de l'OOP)",
            "Kichina" to "在此输入...（例如：总结面向对象编程）"
        )
    )

    fun translate(key: String, language: String): String {
        val entry = translations[key] ?: return key
        return entry[language] ?: entry["Kiswahili"] ?: key
    }
}
